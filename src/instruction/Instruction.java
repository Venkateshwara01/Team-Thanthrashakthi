package instruction;

public class Instruction {
    private final String rawText;
    private String mnemonic = "";
    private String operand1 = "";
    private String operand2 = "";

    public Instruction(String line) {
        this.rawText = line.trim();
        parse();
    }

    private void parse() {
        String clean = rawText.replaceAll(",", " ").replaceAll("\\s+", " ").toUpperCase();
        String[] parts = clean.split(" ");
        this.mnemonic = parts.length > 0 ? parts[0] : "";
        this.operand1 = parts.length > 1 ? parts[1] : "";
        this.operand2 = parts.length > 2 ? parts[2] : "";
    }

    public String getRawText() { return rawText; }
    public String getMnemonic() { return mnemonic; }
    public String getOperand1() { return operand1; }
    public String getOperand2() { return operand2; }
}
