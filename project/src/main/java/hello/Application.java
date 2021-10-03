package hello;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class Application {

    static class Self {
        public String href;
    }

    static class Links {
        public Self self;
    }

    static class PlayerState {
        public Integer x;
        public Integer y;
        public String direction;
        public Boolean wasHit;
        public Integer score;
    }

    static class Arena {
        public List<Integer> dims;
        public Map<String, PlayerState> state;
    }

    static class ArenaUpdate {
        public Links _links;
        public Arena arena;
    }

    class Coordination {
        public Integer x,y;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.initDirectFieldAccess();
    }

    @GetMapping("/")
    public String index() {
        return "Let the battle begin!";
    }

    private Map<Coordination, PlayerState> getStateOnCoordination(ArenaUpdate arenaUpdate) {
        Map<String, PlayerState> map = arenaUpdate.arena.state;
        Map<Coordination, PlayerState> coOrdinationMap = new HashMap<Coordination, PlayerState>();
        map.entrySet().stream().parallel()
            .filter(p ->  ! (p.getKey() == mybot))
            .forEach(
                p -> {
                    Coordination c = new Coordination();
                    c.x = p.getValue().x; 
                    c.y = p.getValue().y;
                    coOrdinationMap.put(c, p.getValue());
                    //System.out.println(p.getKey() + " --> " + p.getValue().x + ":" + p.getValue().y + " - " +  p.getValue().wasHit + " :: " + p.getValue().direction );
                }
        );
        return coOrdinationMap;
    }

    private Coordination getWhoIsInFront(ArenaUpdate arenaUpdate, PlayerState myState, Map<Coordination, PlayerState> stateOnCoordination) {
        for (int i=1; i<=3; i++) {
            if (myState.direction.equals("N")) {
                Coordintation n = new Cordination(myState.x, myState.y-i);
                if (stateOnCoordination.get(n) != null) {
                    return n;
                }
            }
            else if (myState.direction.equals("S")) {
                Coordintation n = new Cordination(myState.x, myState.y+i);
                if (stateOnCoordination.get(n) != null) {
                    return n;
                }
            }
            else if (myState.direction.equals("W")) {
                Coordintation n = new Cordination(myState.x-i, myState.y);
                if (stateOnCoordination.get(n) != null) {
                    return n;
                }
            }
            else if (myState.direction.equals("E")) {
                Coordintation n = new Cordination(myState.x+i, myState.y);
                if (stateOnCoordination.get(n) != null) {
                    return n;
                }
            }
        }
        return null;
    }


    private Coordination getWhoHitMe(ArenaUpdate arenaUpdate, PlayerState myState, Map<Coordination, PlayerState> stateOnCoordination) {
        //check north
        for (int i=1; i<=3; i++) {
            Coordination n = new Cordination(myState.x, myState.y-i);
            if (stateOnCoordination.get(n) != null && stateOnCoordination.get(n).equals("S")) {
                return n;
            }
        }
        //check south
        for (int i=1; i<=3; i++) {
            Coordination n = new Cordination(myState.x, myState.y+i);
            if (stateOnCoordination.get(n) != null && stateOnCoordination.get(n).equals("N")) {
                return n;
            }
        }
        //check west
        for (int i=1; i<=3; i++) {
            Coordination n = new Cordination(myState.x-i, myState.y);
            if (stateOnCoordination.get(n) != null && stateOnCoordination.get(n).equals("E")) {
                return n;
            }
        }
        //check east
        for (int i=1; i<=3; i++) {
            Coordination n = new Cordination(myState.x+i, myState.y);
            if (stateOnCoordination.get(n) != null && stateOnCoordination.get(n).equals("W")) {
                return n;
            }
        }
        return null;
    }

    @PostMapping("/**")
    public String index(@RequestBody ArenaUpdate arenaUpdate) {
        
        String mybot = arenaUpdate._links.self.href;
        System.out.println("Dims : " + arenaUpdate.arena.dims.toString());
        System.out.println("Dims : " + arenaUpdate.arena.dims.get(0) + "," + arenaUpdate.arena.dims.get(1));
        Map<String, PlayerState> map = arenaUpdate.arena.state;

        int x = map.entrySet().stream().parallel()
            .filter(p ->  p.getKey() == mybot)
            .map(p -> p.getValue().x)
            .findAny().orElse(0);
        int y = map.entrySet().stream().parallel()
            .filter(p ->  p.getKey() == mybot)
            .map(p -> p.getValue().y)
            .findAny().orElse(0);
        String d = map.entrySet().stream().parallel()
            .filter(p ->  p.getKey() == mybot)
            .map(p -> p.getValue().direction)
            .findAny().orElse("F");
      
        System.out.println("Bot : x = " + x + " , y = " + y + " , d = " + d);
        
        map.entrySet().stream().parallel()
            .forEach(
                p -> {
                    System.out.println(p.getKey() + " --> " + p.getValue().x + ":" + p.getValue().y + " - " +  p.getValue().wasHit + " :: " + p.getValue().direction );
                }
        );

        PlayerState myState = arenaUpdate.arena.state.get(mybot);
        System.out.println("My co-ordinates : " + myState.x + ", " + myState.y);

        Map<Coordination, PlayerState> stateOnCoordination = getStateOnCoordination(arenaUpdate);

        if (myState.wasHit) {
            Coordination hitMe = getWhoHitMe(arenaUpdate, myState, stateOnCoordination);
            // escape
        }
        else {
            Coordination inFront = getWhoIsInFront(arenaUpdate, myState, stateOnCoordination);
            if (inFront != null) {
                return "T";
            } 
            /*
            else {
                List<Coordination> nearBy = getWhoIsNearby(arenaUpdate, myState, stateOnCoordination);
            }
            */

        }
        /*

        switch(d) {
            case "N":
                map.entrySet().stream().parallel()
                    .filter(p ->  !p.getKey() == mybot)
                    .filter(p ->  p.getValue().y == y)
                    .filter(p ->  p.getValue.x() <= x-3)
                    
                break;
            case "S":
                break;
            case "E":
                break;
            default:
                System.out.println("doNothing");
        }
        */
        
        
        String[] commands = new String[] { "F", "R", "L", "T" };
        int i = new Random().nextInt(4);
        return commands[i];
        //return "F";  // or "T"
    }
}
