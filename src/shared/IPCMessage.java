package shared;

import java.io.Serializable;
import java.util.List;

public class IPCMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final CommandType command;
    private final List<String> programLines;
    private final SystemStateSnapshot stateSnapshot;
    private final String logText;

    public IPCMessage(CommandType command, List<String> programLines, SystemStateSnapshot stateSnapshot, String logText) {
        this.command = command;
        this.programLines = programLines;
        this.stateSnapshot = stateSnapshot;
        this.logText = logText;
    }

    public CommandType getCommand() { return command; }
    public List<String> getProgramLines() { return programLines; }
    public SystemStateSnapshot getStateSnapshot() { return stateSnapshot; }
    public String getLogText() { return logText; }
}