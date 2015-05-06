package mapData;

/**
 * Created by bopablo.g on 2015-05-06.
 */
public class Miasto extends Wspolrzedne {

    public String type;

    public Miasto() {

    }

    public Miasto(int x, int y, String type) {
        super.x  = x;
        super.y  = y;
        this.type = type;
    }

}
