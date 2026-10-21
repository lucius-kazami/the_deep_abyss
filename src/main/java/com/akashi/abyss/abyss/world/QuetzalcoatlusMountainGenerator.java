package com.akashi.abyss.abyss.world;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.entity.ModEntities;
import com.akashi.abyss.abyss.entity.heaven.QuetzalcoatlusEntity;
import com.akashi.abyss.abyss.event.AbyssBiome1ChunkHandler;
import com.akashi.abyss.abyss.event.AbyssTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Chunk-clipped, deterministic giant nesting trees. */
public final class QuetzalcoatlusMountainGenerator {
    private QuetzalcoatlusMountainGenerator() {}
    private static final int CELL = 160;
    private static final int RADIUS = 43;
    private static final int MIN_Y = AbyssBiome1ChunkHandler.FLOOR_Y;
    private static final int MAX_Y = AbyssBiome1ChunkHandler.CEILING_Y;
    private static final BlockState WOOD = Blocks.DARK_OAK_LOG.defaultBlockState();
    private static final BlockState WOOD_X = WOOD.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
    private static final BlockState WOOD_Z = WOOD.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
    private static final BlockState LEAF = Abyss.RED_LEAVES.get().defaultBlockState();
    private record Tree(int x, int z, int base, int height, int nx, int nz, int ny, long seed) {}

    private static long hash(long v) {
        v = (v ^ (v >>> 33)) * 0xff51afd7ed558ccdL;
        v = (v ^ (v >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return v ^ (v >>> 33);
    }
    private static double rand(long seed, int salt) {
        return (hash(seed + salt * 0x9e3779b97f4a7c15L) >>> 11) * 0x1.0p-53;
    }
    private static Tree tree(ServerLevel level, int cx, int cz) {
        long h = hash(level.getSeed() ^ (cx * 341873128712L) ^ (cz * 132897987541L) ^ 0x6D12BEEFL);
        if (Math.floorMod(h >>> 48, 2) != 0) return null;
        int x = cx * CELL + 50 + (int)Math.floorMod(h, 60);
        int z = cz * CELL + 50 + (int)Math.floorMod(h >>> 24, 60);
        AbyssTerrain.Sample s = AbyssTerrain.sample(x, z);
        if (s.water() || s.lava() || AbyssTerrain.isPoisonZone(x, z)) return null;
        int base = s.floorTop();
        int lowestCeiling = Integer.MAX_VALUE;
        for (int dx = -30; dx <= 30; dx += 10)
            for (int dz = -30; dz <= 30; dz += 10) {
                AbyssTerrain.Sample sample = AbyssTerrain.sample(x + dx, z + dz);
                if (sample.water() || sample.lava()) return null;
                lowestCeiling = Math.min(lowestCeiling, AbyssTerrain.ceilingBottom(x + dx, z + dz));
            }
        int height = Math.min(65, lowestCeiling - base - 8);
        if (height < 48) return null;
        double a = rand(h, 4) * Math.PI * 2;
        int nx = x + (int)Math.round(Math.cos(a) * 21);
        int nz = z + (int)Math.round(Math.sin(a) * 21);
        int ny = base + Math.min(36, height - 22);
        return new Tree(x, z, base, height, nx, nz, ny, h);
    }
    private static boolean in(ChunkPos chunk, int x, int z) {
        return (x >> 4) == chunk.x && (z >> 4) == chunk.z;
    }
    private static void put(ServerLevel level, ChunkPos chunk, int x, int y, int z, BlockState state) {
        if (!in(chunk, x, z) || y < MIN_Y || y >= MAX_Y) return;
        BlockPos p = new BlockPos(x, y, z);
        BlockState old = level.getBlockState(p);
        if (level.getBlockEntity(p) != null || old.hasBlockEntity()) return;
        if (old.is(Blocks.BRICKS) || old.is(Blocks.STONE_BRICKS) || old.is(Blocks.MOSSY_STONE_BRICKS)
                || old.is(Blocks.CRACKED_STONE_BRICKS) || old.is(Blocks.IRON_BARS)
                || old.is(Blocks.CHEST) || old.is(Blocks.SPAWNER)) return;
        level.setBlock(p, state, 2);
    }
    private static void ball(ServerLevel level, ChunkPos chunk, double cx, double cy, double cz,
                             double rx, double ry, double rz, BlockState state, long seed, boolean irregular) {
        int x0 = Math.max(chunk.getMinBlockX(), (int)Math.floor(cx - rx - 1));
        int x1 = Math.min(chunk.getMaxBlockX(), (int)Math.ceil(cx + rx + 1));
        int z0 = Math.max(chunk.getMinBlockZ(), (int)Math.floor(cz - rz - 1));
        int z1 = Math.min(chunk.getMaxBlockZ(), (int)Math.ceil(cz + rz + 1));
        for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++)
            for (int y = (int)Math.floor(cy - ry - 1); y <= (int)Math.ceil(cy + ry + 1); y++) {
                double d = Math.pow((x-cx)/rx,2) + Math.pow((y-cy)/ry,2) + Math.pow((z-cz)/rz,2);
                double wobble = irregular ? 0.82 + rand(seed ^ (x*73428767L) ^ (z*912367L), y) * 0.36 : 1;
                if (d <= wobble) put(level, chunk, x, y, z, state);
            }
    }
    private static void limb(ServerLevel level, ChunkPos chunk, double ax, double ay, double az,
                             double bx, double by, double bz, double radius, BlockState state) {
        int steps = Math.max(1, (int)Math.ceil(Math.sqrt(Math.pow(bx-ax,2)+Math.pow(by-ay,2)+Math.pow(bz-az,2))*1.5));
        for (int i = 0; i <= steps; i++) {
            double t = i/(double)steps;
            double x = ax + (bx-ax)*t, y = ay+(by-ay)*t, z = az+(bz-az)*t;
            double r = Math.max(0.9, radius * (1 - 0.65*t));
            ball(level, chunk, x, y, z, r, r, r, state, 0, false);
        }
    }
    private static void draw(ServerLevel level, ChunkPos chunk, Tree t) {
        // Wide, twisting buttress roots, tapered naturally into the soil.
        for (int i=0;i<8;i++) {
            double a = i*Math.PI/4 + rand(t.seed, i+20)*0.35;
            double len = 10 + rand(t.seed, i+40)*7;
            double ex = t.x + Math.cos(a)*len, ez = t.z + Math.sin(a)*len;
            int ground = AbyssTerrain.sample((int)Math.round(ex),(int)Math.round(ez)).floorTop();
            limb(level,chunk,t.x,t.base+5,t.z,ex,ground-1,ez,3.2,WOOD);
        }
        // Crooked trunk built from overlapping tapered sections.
        double px=t.x, pz=t.z;
        for (int j=0;j<7;j++) {
            double a=j/7.0;
            double q=(j+1)/7.0;
            double x=t.x+Math.sin(q*3.5+t.seed%17)*q*2.2;
            double z=t.z+Math.cos(q*3.1+t.seed%13)*q*2.0;
            limb(level,chunk,px,t.base+a*t.height,pz,x,t.base+q*t.height,z,5.2-3.0*a,WOOD);
            px=x; pz=z;
        }
        // Asymmetric branches and independent red leaf clusters.
        for (int i=0;i<11;i++) {
            double angle=i*2.399963 + rand(t.seed,i+90)*0.55;
            double start=t.base+t.height*(0.40+rand(t.seed,i+110)*0.43);
            double length=14+rand(t.seed,i+130)*16;
            double sx=t.x+Math.sin((start-t.base)/t.height*3.5+t.seed%17)*1.5;
            double sz=t.z+Math.cos((start-t.base)/t.height*3.1+t.seed%13)*1.5;
            double ex=sx+Math.cos(angle)*length, ez=sz+Math.sin(angle)*length;
            double ey=Math.min(t.base+t.height-7,start+5+rand(t.seed,i+150)*9);
            limb(level,chunk,sx,start,sz,(sx+ex)/2,start+3,(sz+ez)/2,2.8,WOOD);
            limb(level,chunk,(sx+ex)/2,start+3,(sz+ez)/2,ex,ey,ez,1.8,
                    Math.abs(ex-sx)>Math.abs(ez-sz)?WOOD_X:WOOD_Z);
            if (Math.hypot(ex-t.nx,ez-t.nz)>14 || Math.abs(ey-t.ny)>12) {
                ball(level,chunk,ex,ey+2,ez,8+rand(t.seed,i+170)*4,5+rand(t.seed,i+180)*3,
                        8+rand(t.seed,i+190)*4,LEAF,t.seed+i,true);
            }
        }
        ball(level,chunk,px,t.base+t.height-2,pz,10,7,10,LEAF,t.seed+333,true);
        // Thick supporting branch under the open nest.
        limb(level,chunk,t.x,t.ny-5,t.z,t.nx,t.ny-3,t.nz,4.5,WOOD);
        ball(level,chunk,t.nx,t.ny-2,t.nz,10,2,10,WOOD,t.seed,false);
        for (int x=Math.max(chunk.getMinBlockX(),t.nx-10);x<=Math.min(chunk.getMaxBlockX(),t.nx+10);x++)
            for (int z=Math.max(chunk.getMinBlockZ(),t.nz-10);z<=Math.min(chunk.getMaxBlockZ(),t.nz+10);z++) {
                double d=Math.hypot(x-t.nx,z-t.nz);
                if (d<6) put(level,chunk,x,t.ny,z,Blocks.MOSS_BLOCK.defaultBlockState());
                else if (d<8.5) put(level,chunk,x,t.ny,z,WOOD);
                if (d>=7 && d<9.5 && rand(t.seed ^ (x*341873128712L) ^ z,3)>0.18)
                    put(level,chunk,x,t.ny+1,z,rand(t.seed ^ x ^ (z*31L),7)<0.7?WOOD:LEAF);
            }
        // Keep the central nest open; no overhead foliage near the egg.
        for(int x=Math.max(chunk.getMinBlockX(),t.nx-5);x<=Math.min(chunk.getMaxBlockX(),t.nx+5);x++)
            for(int z=Math.max(chunk.getMinBlockZ(),t.nz-5);z<=Math.min(chunk.getMaxBlockZ(),t.nz+5);z++)
                if(Math.hypot(x-t.nx,z-t.nz)<5)
                    for(int y=t.ny+1;y<=t.ny+12;y++) {
                        BlockPos p=new BlockPos(x,y,z);
                        if(level.getBlockState(p).is(LEAF.getBlock()) || level.getBlockState(p).is(WOOD.getBlock()))
                            put(level,chunk,x,y,z,Blocks.AIR.defaultBlockState());
                    }
    }
    public static void carve(ServerLevel level, ChunkPos chunk) {
        int x0=Math.floorDiv(chunk.getMinBlockX()-RADIUS,CELL),x1=Math.floorDiv(chunk.getMaxBlockX()+RADIUS,CELL);
        int z0=Math.floorDiv(chunk.getMinBlockZ()-RADIUS,CELL),z1=Math.floorDiv(chunk.getMaxBlockZ()+RADIUS,CELL);
        for(int cx=x0;cx<=x1;cx++) for(int cz=z0;cz<=z1;cz++) {
            Tree t=tree(level,cx,cz);
            if(t!=null && chunk.getMinBlockX()<=t.x+RADIUS && chunk.getMaxBlockX()>=t.x-RADIUS
                    && chunk.getMinBlockZ()<=t.z+RADIUS && chunk.getMaxBlockZ()>=t.z-RADIUS) draw(level,chunk,t);
        }
    }
    public static void populate(ServerLevel level, ChunkPos chunk) {
        int x0=Math.floorDiv(chunk.getMinBlockX()-RADIUS,CELL),x1=Math.floorDiv(chunk.getMaxBlockX()+RADIUS,CELL);
        int z0=Math.floorDiv(chunk.getMinBlockZ()-RADIUS,CELL),z1=Math.floorDiv(chunk.getMaxBlockZ()+RADIUS,CELL);
        for(int cx=x0;cx<=x1;cx++) for(int cz=z0;cz<=z1;cz++) {
            Tree t=tree(level,cx,cz);
            if(t==null || !in(chunk,t.nx,t.nz)) continue;
            BlockPos egg=new BlockPos(t.nx,t.ny+1,t.nz);
            if(level.getBlockState(egg).is(Abyss.QUETZALCOATLUS_EGG.get())) continue;
            if(level.getBlockEntity(egg)!=null || level.getBlockEntity(egg.below())!=null) continue;
            level.setBlock(egg.below(),Blocks.MOSS_BLOCK.defaultBlockState(),3);
            for(int y=0;y<4;y++) {
                BlockPos p=egg.above(y);
                if(level.getBlockEntity(p)!=null) return;
            }
            for(int y=0;y<4;y++) level.setBlock(egg.above(y),Blocks.AIR.defaultBlockState(),3);
            level.setBlock(egg,Abyss.QUETZALCOATLUS_EGG.get().defaultBlockState(),3);
            QuetzalcoatlusEntity adult=ModEntities.QUETZALCOATLUS.get().create(level,EntitySpawnReason.CHUNK_GENERATION);
            if(adult!=null) {
                adult.snapTo(t.nx+6.5,t.ny+2,t.nz+0.5,180,0);
                if(level.noCollision(adult)) {adult.setPersistenceRequired();level.addFreshEntity(adult);}
            }
        }
    }
}
