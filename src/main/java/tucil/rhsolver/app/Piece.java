package tucil.rhsolver.app;

import java.util.ArrayList;
import java.util.List;

public class Piece {
    private Character id;
    private List<Coords> position;

    public Piece(char id) {
        this.id = id;
        this.position = new ArrayList<>();
    }

    public Piece(Piece piece) {
        this.id = piece.id;
        this.position = new ArrayList<>();
        for (Coords coord : piece.position) {
            this.position.add(new Coords(coord.getX(), coord.getY()));
        }
    }

    public Character getId() {
        return id;
    }

    public List<Coords> getPosition() {
        return position;
    }

    public void addCoord(Coords coord) {
        position.add(coord);
    }

    public Boolean isHorizontal() {
        if (position.size() < 2) return true;
        return position.get(0).getX() == position.get(1).getX();
    }

    public void move(boolean forward) {
        int mult = forward ? 1 : -1;
        if (isHorizontal()) {
            for (Coords coord : position) {
                coord.addY(mult);
            }
        } else {
            for (Coords coord : position) {
                coord.addX(mult);
            }
        }
    }

    public boolean isIntersecting(Coords other) {
        for (Coords coord : position) {
            if (coord.isIntersecting(other)) {
                return true;
            }
        }
        return false;
    }

    public void debugPrint() {
        System.out.print("Piece " + id + ": ");
        for (Coords coord : position) {
            System.out.print(coord.toString() + " ");
        }
        System.out.println();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(":");
        for (Coords coord : position) {
            sb.append(coord.getX()).append(":").append(coord.getY()).append(";");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return id == piece.id;
    }
}
