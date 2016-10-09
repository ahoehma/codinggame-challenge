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
        static P of(float x, float y) { P np = new P(); np.x=x; np.y=y; return np; };
        public String toString() { return "P("+x+","+y+")"; }
    };
    
    static class Node {
        P pos;
    }
    
    static class DP extends Node {
        float id;
        float idx;
        static DP of(int idx, int id, float x, float y) { DP ndp=new DP(); ndp.idx=idx; ndp.id=id; ndp.pos=P.of(x,y); return ndp; }
        public String toString() { return "DP("+id+")"; }
    }
    
    static class E extends Node {
        int id;
        int idx;
        int life;
        static E of(int idx, int id, float x, float y, int life) { E ne=new E(); ne.idx=idx; ne.id=id; ne.pos=P.of(x,y); ne.life=life; return ne; }
        public String toString() { return "E("+id+"/life:"+life+")"; }
    }
    
    static class Graph {
        float distance(Node a, Node b) { return P.dist(a.pos, b.pos); }
    }
    
    static class Distance<A extends Node, B extends Node> {
        A a;
        B b;
        float dist;
        Distance (A a, B b) { this.a=a; this.b=b; this.dist=P.dist(a.pos,b.pos); }
        public String toString() { return a+" <= "+dist+" => "+b; }
    }
    
    static class Damage extends Distance<E, E> {
        float damage;
        Damage (E a, E b) { super(a,b); this.damage=damage(dist); }
        public String toString() { return a+" <= "+dist+"/"+damage+" => "+b; }
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
        game_loop: while (true) {
            
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
            
            List<E> skipEnemy = new ArrayList<>();
            
            int loop = 0;
            
            while(skipEnemy.size() < e.size()) {
            
                // until unchecked enemies
                loop++;
                System.err.println("Loop "+loop);
            
                Distance<E, DP> minDistE2DP = e2dp_Dist.stream().filter(d->!skipEnemy.contains(d.a)).min((d1,d2)->Float.compare(d1.dist, d2.dist)).orElse(null);
                Damage maxDamage = me2e_Damage.stream().filter(d->!skipEnemy.contains(d.a)).max((d1,d2)->Float.compare(d1.damage, d2.damage)).orElse(null);
                
                System.err.println("Min distance e to dp: "+minDistE2DP);
                System.err.println("Max damage me to e: "+maxDamage);
                
                if (minDistE2DP.a == maxDamage.b) {
                    // perfect match :D 
                    // enemy is close to dp and we have max damage
                    shoot(maxDamage.b);
                    continue game_loop;
                }
                
                Damage myDamageMe2E = me2e_Damage.stream().filter(d->d.b==minDistE2DP.a).findFirst().orElse(null);
                int steps_e_2_dp = (int)minDistE2DP.dist/500;  // 500 dist per enemy move
                int steps_e_2_kill = (int)(myDamageMe2E.b.life/myDamageMe2E.damage);
                if (steps_e_2_dp <= steps_e_2_kill) {
                    // no chance to kill the enemy before he take the dp
                    skipEnemy.add(minDistE2DP.a);
                    System.err.println("Ignore '"+myDamageMe2E.b+"'");
                    System.err.println("\tMy damage to e: "+myDamageMe2E);
                    System.err.println("\tSteps for e to dp - steps: "+steps_e_2_dp+", "+minDistE2DP);
                    System.err.println("\tSteps to kill e - steps: "+steps_e_2_kill+", "+myDamageMe2E);
                    continue;
                }
                
                if (safe_place2go(minDistE2DP.a.pos)) {
                    // a enemy is close to that dp, try to go to the enemy to increase our damage
                    go(minDistE2DP.a);
                    System.err.println("Go to '"+minDistE2DP.a+"'");
                    continue game_loop;
                }
                if (safe_place2go(minDistE2DP.b.pos)) {
                    // a enemy is close to that dp, try to go to the dp to increase our damage
                    go(minDistE2DP.b);
                    System.err.println("Go to '"+minDistE2DP.b+"'");
                    continue game_loop;
                }
                
                Distance<E, E> minDistE = me2e_Dist.stream().filter(d->!skipEnemy.contains(d.a)).min((d1,d2)->Float.compare(d1.dist, d2.dist)).orElse(null);
                System.err.println("Min distance e to me: "+minDistE);
                
                if (minDistE.b == maxDamage.b) {
                    // perfect match :D 
                    // enemy is close to me and we have max damage
                    shoot(maxDamage.b);
                    continue game_loop;
                }
                
                shoot(minDistE.b);
                continue game_loop;
            }
        }
    }
    
    // check if its safe to walk in the direction to this enemy
    static boolean safe_place2go(P pos) {
        
        // check my next position agains all "next" enemies positions
        E nextMe = getPointInBetweenByLen(me.pos, pos, 1000); // I can go 1000 per step
        List<Distance<E, E>> _me2e_Dist = new ArrayList<>();
        for (int i = 0; i < enemyCount; i++) {
            // here we can try to simulate the next pos of the enemy
            E _e = e.get(i);
            Distance<E, DP> closestDP4E = e2dp_Dist.stream().filter(d->d.a==_e).min((d1,d2)->Float.compare(d1.dist, d2.dist)).orElse(null);
            E nextE = getPointInBetweenByLen(_e.pos, closestDP4E.b.pos, 500); // enemy can go 500 per step
            _me2e_Dist.add(new Distance<E, E>(nextMe, nextE)); 
        }
        List<Distance<E, E>> dangers = _me2e_Dist.stream().filter(d->d.dist<=2000).collect(Collectors.toList()); // 2000 danger zone
        if (!dangers.isEmpty()) {
            System.err.println("Not save to go to: "+nextMe.pos);
            dangers.forEach(d->System.err.println("\tEnemy '"+d.b+"' is too close"));
            return false;
        }
        
        return true;
    }
    
    static void shoot(E e) {System.out.println("SHOOT "+e.id);}
    static void go(Node n) {System.out.println("MOVE "+(int)n.pos.x+" "+(int)n.pos.y);}
    
    static float damage(float distance) {return (float)(125000f/Math.pow(distance, 1.2f));}
    
    static int h(int idx) {return e.get(idx).life;}
    static String e(int idx) {return e.get(idx).toString(); }
    static String dp(int idx) {return dp.get(idx).toString(); }
    
    // arithmetics 
    static E getPointInBetweenByLen(P a, P b, int length) {
        P nextP = a.cp().add(b.cp().sub(a).norm().mul(length));
        return E.of(-1, -1, nextP.x, nextP.y, Integer.MAX_VALUE);
    }

}
