package mapData;

import java.util.HashMap;

/**
 * Created by bopablo.g on 2015-05-06.
 */
public class Node extends Wspolrzedne {

    public long id = 0;

    public HashMap<String, String> atrybuty;

    private int skrzyzowanie = 0;


    public Node(long id, int x, int y){

        this.id = id;
        super.x = x;
        super.y = y;
    }

    public void nalezyDoDroga() {

		// node jest czescia kilku ulic (skrzyzowanie)
		this.skrzyzowanie++;
    }

    public boolean czySkrzyzowanie() {

        if (skrzyzowanie > 1)
            return true;

        return false;
    }

    public void addAtrybut(String key, String value){

        if (this.atrybuty == null)
            this.atrybuty = new HashMap<String,String>();

        this.atrybuty.put(key, value);
    }

    public String getAtrybut(String key){

        if (this.atrybuty == null)
            return null;

        return (String)this.atrybuty.get(key);
    }

}
