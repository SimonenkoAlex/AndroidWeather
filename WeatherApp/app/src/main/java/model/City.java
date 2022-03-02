package model;

public class City {
    private long id;
    private String name;
    private int avgTemp;

    public City(long id, String name, int avgTemp){
        this.id = id;
        this.name = name;
        this.avgTemp = avgTemp;
    }
    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAvgTemp() {
        return avgTemp;
    }

    public void setAvgTemp(int avgTemp) {
        this.avgTemp = avgTemp;
    }

    @Override
    public String toString() {
        return this.name + " : " + this.avgTemp;
    }
}
