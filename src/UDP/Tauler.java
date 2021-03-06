package UDP;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Tauler implements Serializable {
    public static final long serialVersionUID = 1L;
    public Map<String,Integer> map_jugadors;
    public int resultat = 3, acabats, num_tiradas;

    String[][] tauler = { {" 1 "," 2 "," 3 "," 4 "," 5 "," 6 "," 7 "},{" ~ ", " ~ ", " ~ ", " ~ ", " ~ ", " ~ ", " ~ "},
            {" ~ ", " ~ ", " ~ ", " ~ ", " ~ ", " ~ ", " ~ "},{" ~ ", " ~ ", " ~ ", " ~ ", " ~ ", " ~ ", " ~ "},
            {" ~ ", " ~ ", " ~ ", " ~ ", " ~ ", " ~ ", " ~ "},{" ~ ", " ~ ", " ~ ", " ~ ", " ~ ", " ~ ", " ~ "},
            {" ~ ", " ~ ", " ~ ", " ~ ", " ~ ", " ~ ", " ~ "}};

    public Tauler() {
        map_jugadors = new HashMap<>();
        acabats = 0;
        num_tiradas = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Intents\n");
        map_jugadors.forEach((k,v) -> sb.append(k + " - " + v + "\n"));
        return sb.toString();
    }
}

class Jugada implements Serializable {
    public static final long serialVersionUID = 1L;
    String Nom;
    int num;
    String marca;
}