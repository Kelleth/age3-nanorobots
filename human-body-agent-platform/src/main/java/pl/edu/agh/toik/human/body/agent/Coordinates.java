package pl.edu.agh.toik.human.body.agent;

/**
 * Created by Kuba on 17.05.15.
 */
public class Coordinates {
    private double xCoordinate;
    private double yCoordinate;

    public Coordinates(double yCoordinate, double xCoordinate) {
        this.yCoordinate = yCoordinate;
        this.xCoordinate = xCoordinate;
    }

    public double getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(double xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public double getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(double yCoordinate) {
        this.yCoordinate = yCoordinate;
    }
}
