import java.util.*;
import java.io.*;
import java.math.*;

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
        float distance(Node a, Node b) { return P.dist(a.pos, b.pos); };
    }
    
    class Distance<A extends Node, B extends Node> {
        A a;
        B b;
        float dist;
        static <A extends Node, B extends Node> Distance of(A a, B b){ Distance<A,B> d = new Distance<>(); d.a=a; d.b=b; d.dist=P.dist(a.pos,b.pos); return d; };
    }
    
    class Damage extends Distance<E, E> {
        float damage;
        static Damage of(E a, E b){ Damage d = new Damage<>(a, b); d.damage=damage(d.dist); return d; };
    }
    
    static DP[] dp;
    static E[] e;
    static Distance<E, E>[] me2e_Dist;
    static Distance<E, DP>[] me2dp_Dist;
    static Distance<E, DP>[] e2dp_Dist;
    static Damage[] me2e_Damage;
    
    static P me;
    
    static int enemyCount;

    public static void main(String args[]) {
        
        Scanner in = new Scanner(System.in);
        
        // 16 x 9
    
        // game loop
        while (true) {
            
            // read input
            int x = in.nextInt();
            int y = in.nextInt();
            me = P.of(x,y);
            int dataCount = in.nextInt();
            dp=new DP[dataCount];
            for (int i = 0; i < dataCount; i++) {
                int dataId = in.nextInt();
                int dataX = in.nextInt();
                int dataY = in.nextInt();
                dp[i]=DP.of(i, dataId, dataX, dataY);
            }
            enemyCount = in.nextInt();
            e=new E[enemyCount];
            for (int i = 0; i < enemyCount; i++) {
                int enemyId = in.nextInt();
                int enemyX = in.nextInt();
                int enemyY = in.nextInt();
                int enemyLife = in.nextInt();
                e[i]=E.of(i, enemyId, enemyX, enemyY, enemyLife);
            }

            // calculate distances
            e2dp_Dist=new Distance<>[enemyCount*dataCount];
            me2dp_Dist=new Distance<>[dataCount];
            me2e_Dist=new Distance<>[enemyCount];
            me2e_Damage=new Damage<>[enemyCount];
            for (int j = 0; j < dataCount; j++) {
                for (int i = 0; i < enemyCount; i++) {
                    e2dp_Dist[i*j]=Distance.of(e[i], dp[j]);
                }
                me2dp_Dist[j]=Distance.of(me, dp[j]);
            }
            for (int i = 0; i < enemyCount; i++) {
                me2e_Dist[i]=Distance.of(me, e[i]);
                // calculate damages too
                me2e_Damage[i]=Distance.of(me, e[i]);
            }
            
            int e_dist_dp = e2dp_Dist[closestDistE2DP.idx][closestDistDP.idx];
            int steps_e_2_dp = e_dist_dp/500;  // 500 dist per enemy move
            int steps_e_2_kill = (int)(closestDistE2DP.life/damage);
            
            System.err.println("Steps for e to dp - steps: "+steps_e_2_dp+", dist: "+e2dp_Dist[closestDistE2DP.idx][closestDistDP.idx]);
            System.err.println("Steps to kill e - steps: "+steps_e_2_kill+", damage: "+damage+", dist: "+e2me_Dist[closestDistE2DP.idx]);
            
            
            if (steps_e_2_dp < steps_e_2_kill && safe_distance(closestDistE2DP.idx) && safe_place_if_go2e(closestDistE2DP.idx)) {
                go2e(closestDistE2DP.idx);
                continue;
            }
            
            if (e2dp_Dist[closestDistE2DP.idx][closestDistDP.idx]>=2500 && safe_distance(closestDistE2DP.idx)) {
                // e more than 4 steps away from dp
                // e 6 steps away from me
                go2e(closestDistE2DP.idx);
                continue;
            }
            
            shoot(closestDistE2DP.idx);

            //System.out.println("MOVE 8000 4500"); // MOVE x y or SHOOT id
        }
    }
    
    // enought safe distance between me and this enemy?
    static boolean safe_distance(int idx) {return e2me_Dist[idx]>3000;}
    
    // check if its safe to walk in the direction to this enemy
    static boolean safe_place_if_go2e(int idx) {
        
        // check my next position agains all current enemies positions
        P nextMe = getPointInBetweenByLen(me, e[idx].pos, 2000); // 1000 dist per my move
        int[] e2me_Dist=new int[enemyCount];
        for (int i = 0; i < enemyCount; i++) {
            int iDist=e2me_Dist[i]=(int)P.dist(e[i].pos, nextMe);
            if (iDist<2000) {
                System.err.println("Enemy '"+e[i]+"' is not near if we go to next pos '"+nextMe+"' from '"+me+"'");
                return false;
            }
        }

        // TODO: check my next position agains all next enemies position (assume that each enemy go the "his" nex datapoint)
        
        return true;
    }
    
    static void shoot(int idx) {System.out.println("SHOOT "+e[idx].id);}
    static void go2e(int idx) {System.out.println("MOVE "+(int)e[idx].pos.x+" "+(int)e[idx].pos.y);}
    
    static double damage(float distance) {return 125000f/Math.pow(distance, 1.2f);}
    
    static int h(int idx) {return e[idx].life;}
    static String e(int idx) {return e[idx].toString(); }
    static String dp(int idx) {return dp[idx].toString(); }
    
    // arithmetics 
    static P getPointInBetweenByLen(P a, P b, int length) {
        return a.cp().add(b.cp().sub(a).norm().mul(length));
    }

}
