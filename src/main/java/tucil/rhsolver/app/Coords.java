package tucil.rhsolver.app;

public class Coords {
    private int x;
    private int y;

    public Coords(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coords() {
        this.x = 0;
        this.y = 0;
    }

    public Coords(Coords other) {
        this.x = other.x;
        this.y = other.y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void addX(int x) {
        this.x += x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void addY(int y) {
        this.y += y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double distanceTo(Coords other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    public boolean isIntersecting(Coords other) {
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public String toString() {
        return "Coords{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
