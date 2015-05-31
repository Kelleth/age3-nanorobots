package pl.edu.agh.toik.human.body.agent;

/**
 * Created by Kuba on 17.05.15.
 */
public class Coordinates {
    private double xCoordinate;
    private double yCoordinate;

    public Coordinates(double xCoordinate, double yCoordinate) {
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

    public double calculateDistanceTo(Coordinates coordinates) {
        return Math.sqrt(Math.pow(xCoordinate - coordinates.getxCoordinate(), 2) + Math.pow(yCoordinate - coordinates.getyCoordinate(), 2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinates that = (Coordinates) o;

        if (Double.compare(that.xCoordinate, xCoordinate) != 0) return false;
        return Double.compare(that.yCoordinate, yCoordinate) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(xCoordinate);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(yCoordinate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
