package quark.mining;

public class MiningDriver {
  public static void main(String args[]) {
    ProcessHandle.allProcesses().filter(h -> h.info().command().isPresent())
        .filter(h -> h.info().command().get().contains("bitcoin"))
        .forEach(handle -> System.out.println(handle.info()));
  }
  public void startMiner(){
    String minerCommand = "C:/Users/starf/Downloads/bitcoinmining/ccminer-818-cuda91-x64/ccminer.exe";
    new ProcessBuilder(minerCommand);
  }
}
