package dao;

public class Formation {
    private String name;

    public Formation() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Formation{" +
                "name='" + name + '\'' +
                '}';
    }
}
