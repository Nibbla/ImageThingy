import java.util.logging.Logger;

/**
 * Created by Niebisch Markus on 25.03.2018.
 */
public class Sound {
    static final Logger logger = Logger.getLogger(Sound.class.getName());

    private int myHerz;
    private double myStartInSekunden;
    private double myLängeInSekunden;
    private double myLautstärkeInProzent;

    public Sound(int myHerz, double myStartInSekunden, double myLängeInSekunden, double myLautstärkeInProzent) {
        this.myHerz = myHerz;
        this.myStartInSekunden = myStartInSekunden;
        this.myLängeInSekunden = myLängeInSekunden;
        this.myLautstärkeInProzent = myLautstärkeInProzent;
    }

    public int getMyHerz() {
        return myHerz;
    }

    public void setMyHerz(int myHerz) {
        this.myHerz = myHerz;
    }

    public double getMyStartInSekunden() {
        return myStartInSekunden;
    }

    public void setMyStartInSekunden(double myStartInSekunden) {
        this.myStartInSekunden = myStartInSekunden;
    }

    public double getMyLängeInSekunden() {
        return myLängeInSekunden;
    }

    public void setMyLängeInSekunden(double myLängeInSekunden) {
        this.myLängeInSekunden = myLängeInSekunden;
    }

    public double getMyLautstärkeInProzent() {
        return myLautstärkeInProzent;
    }

    public void setMyLautstärkeInProzent(double myLautstärkeInProzent) {
        this.myLautstärkeInProzent = myLautstärkeInProzent;
    }
}
