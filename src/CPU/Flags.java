package cpu;

public class Flags {
    private boolean cy = false;
    private boolean ac = false;
    private boolean ov = false;
    private boolean p = false;

    public void reset() {
        cy = ac = ov = p = false;
    }

    public boolean isCy() { return cy; }
    public void setCy(boolean cy) { this.cy = cy; }

    public boolean isAc() { return ac; }
    public void setAc(boolean ac) { this.ac = ac; }

    public boolean isOv() { return ov; }
    public void setOv(boolean ov) { this.ov = ov; }

    public boolean isP() { return p; }

    public void updateParity(int acc) {
        int count = Integer.bitCount(acc & 0xFF);
        this.p = (count % 2 != 0);
    }

    @Override
    public String toString() {
        return String.format("CY: %b | AC: %b | OV: %b | P: %b", cy, ac, ov, p);
    }
}