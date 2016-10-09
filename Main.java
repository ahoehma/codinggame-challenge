import java.util.*;
import java.io.*;
import java.math.*;
import java.util.stream.*;

/**
 * Shoot enemies before they collect all the incriminating data!
 * The closer you are to an enemy, the more damage you do but don't get too close or you'll get killed.
 **/
class Player {
    
    static class P  {
        float x,y;
        P cp() { P np = new P(); np.x=x; np.y=y; return np;}
        P add(P p) { x+=p.x; y+=p.y; return this;}
        P sub(P p) { x-=p.x; y-=p.y; return this;}
        P norm() { float l = lenght(); if (l != 0) {x /= l;y /= l;} return this; }
        P mul(int s) { x*=s; y*=s; return this; }
        float lenght() { return (float) Math.sqrt(x*x+y*y);}
        static float dist(P a, P b){ return a.cp().sub(b).lenght(); };
        static P of(int x, int y) { P np = new P(); np.x=x; np.y=y; return np; };
    };
    
    static class Node {
        P pos;
    }
    
    static class DP extends Node {
        int id;
        int idx;
        static DP of(int idx, int id, int x, int y) { DP ndp=new DP(); ndp.idx=idx; ndp.id=id; ndp.pos=P.of(x,y); return ndp; }
        public String toString() { return id+""; }
    }
    
    static class E extends Node {
        int id;
        int idx;
        int life;
        static E of(int idx, int id, int x, int y, int life) { E ne=new E(); ne.idx=idx; ne.id=id; ne.pos=P.of(x,y); ne.life=life; return ne; }
        public String toString() { return id+"("+life+")"; }
    }
    
    static class Graph {
        float distance(Node a, Node b) { return P.dist(a.pos, b.pos); }
    }
    
    static class Distance<A extends Node, B extends Node> {
        A a;
        B b;
        float dist;
        Distance (A a, B b) { this.a=a; this.b=b; this.dist=P.dist(a.pos,b.pos); }
        public String toString() { return a+"("+dist+")"+b; }
    }
    
    static class Damage extends Distance<E, E> {
        float damage;
        Damage (E a, E b) { super(a,b); this.damage=damage(dist); }
        public String toString() { return a+"<-"+dist+"/"+damage+"->"+b; }
    }
    
    static List<DP> dp = new ArrayList<>();
    static List<E> e = new ArrayList<>();
    static List<Distance<E, DP>> me2dp_Dist = new ArrayList<>();
    static List<Distance<E, DP>> e2dp_Dist = new ArrayList<>();
    static List<Distance<E, E>> me2e_Dist = new ArrayList<>();
    static List<Damage> me2e_Damage = new ArrayList<>();
    
    static E me;
    
    static int enemyCount;

    public static void main(String args[]) {
        
        Scanner in = new Scanner(System.in);
        
        // 16 x 9
    
        // game loop
        while (true) {
            
            dp.clear();
            e.clear();
            me2e_Dist.clear();
            me2dp_Dist.clear();
            e2dp_Dist.clear();
            me2e_Damage.clear();
            
            // read input
            int x = in.nextInt();
            int y = in.nextInt();
            me = E.of(-1, -1, x, y, Integer.MAX_VALUE);
            int dataCount = in.nextInt();
            for (int i = 0; i < dataCount; i++) {
                int dataId = in.nextInt();
                int dataX = in.nextInt();
                int dataY = in.nextInt();
                dp.add(DP.of(i, dataId, dataX, dataY));
            }
            enemyCount = in.nextInt();
            for (int i = 0; i < enemyCount; i++) {
                int enemyId = in.nextInt();
                int enemyX = in.nextInt();
                int enemyY = in.nextInt();
                int enemyLife = in.nextInt();
                e.add(E.of(i, enemyId, enemyX, enemyY, enemyLife));
            }

            // calculate distances
            for (int j = 0; j < dataCount; j++) {
                for (int i = 0; i < enemyCount; i++) {
                    e2dp_Dist.add(new Distance<E, DP>(e.get(i), dp.get(j)));
                }
                me2dp_Dist.add(new Distance<E, DP>(me, dp.get(j)));
            }
            for (int i = 0; i < enemyCount; i++) {
                me2e_Dist.add(new Distance<E, E>(me, e.get(i)));
                // calculate damages too
                me2e_Damage.add(new Damage(me, e.get(i)));
            }
            
            Distance<E, DP> minDistE2DP = e2dp_Dist.stream().min((d1,d2)->Float.compare(d1.dist, d2.dist)).orElse(null);
            Damage maxDamageMe2E = me2e_Damage.stream().max((d1,d2)->Float.compare(d1.damage, d2.damage)).orElse(null);
            
            System.err.println("Min distance: "+minDistE2DP);
            System.err.println("Max damage: "+maxDamageMe2E);
            
            //int steps_e_2_dp = (int)minDistE2DP.dist/500;  // 500 dist per enemy move
            //int steps_e_2_kill = (int)(maxDamageMe2E.a.life/maxDamageMe2E.damage);
            
            //System.err.println("Steps for e to dp - steps: "+steps_e_2_dp+", dist: "+minDistE2DP.dist);
            //System.err.println("Steps to kill e - steps: "+steps_e_2_kill+", damage: "+maxDamageMe2E.damage+", dist: "+maxDamageMe2E.dist);
            
            
            //if (steps_e_2_dp < steps_e_2_kill && safe_distance(closestDistE2DP.idx) && safe_place_if_go2e(closestDistE2DP.idx)) {
            //    go2e(closestDistE2DP.idx);
            //    continue;
            //}
            
            //if (e2dp_Dist[closestDistE2DP.idx][closestDistDP.idx]>=2500 && safe_distance(closestDistE2DP.idx)) {
                // e more than 4 steps away from dp
                // e 6 steps away from me
            //    go2e(closestDistE2DP.idx);
            //    continue;
            //}
            
            //shoot(closestDistE2DP.idx);

            System.out.println("MOVE 8000 4500"); // MOVE x y or SHOOT id
        }
    }
    
    // enought safe distance between me and this enemy?
    static boolean safe_distance(int idx) {return me2e_Dist.get(idx).dist>3000;}
    
    // check if its safe to walk in the direction to this enemy
    static boolean safe_place_if_go2e(int idx) {
        
        // check my next position agains all current enemies positions
        P nextMe = getPointInBetweenByLen(me.pos, e.get(idx).pos, 1000); // 1000 dist per my move
        List<Distance<E, E>> _me2e_Dist = new ArrayList<>();
        List<Damage> _me2e_Damage = new ArrayList<>();
        for (int i = 0; i < enemyCount; i++) {
            _me2e_Dist.add(new Distance<E, E>(me, e.get(i)));
            // calculate damages too
            _me2e_Damage.add(new Damage(me, e.get(i)));
        }
        List<Distance<E, E>> dangers = _me2e_Dist.stream().filter(d->d.dist<=2000).collect(Collectors.toList());
        if (!dangers.isEmpty()) {
            dangers.forEach(d->System.err.println("Enemy '"+d.b+"' is too near"));
            return false;
        }

        // TODO: check my next position agains all next enemies position (assume that each enemy go the "his" nex datapoint)
        
        return true;
    }
    
    static void shoot(int idx) {System.out.println("SHOOT "+e.get(idx).id);}
    static void go2e(int idx) {System.out.println("MOVE "+(int)e.get(idx).pos.x+" "+(int)e.get(idx).pos.y);}
    
    static float damage(float distance) {return (float)(125000f/Math.pow(distance, 1.2f));}
    
    static int h(int idx) {return e.get(idx).life;}
    static String e(int idx) {return e.get(idx).toString(); }
    static String dp(int idx) {return dp.get(idx).toString(); }
    
    // arithmetics 
    static P getPointInBetweenByLen(P a, P b, int length) {
        return a.cp().add(b.cp().sub(a).norm().mul(length));
    }

}
