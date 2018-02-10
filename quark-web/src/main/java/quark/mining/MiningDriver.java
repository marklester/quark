package quark.mining;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * TODO control wallets 
 * TODO control miner 
 * TODO timeshare miner 
 * TODO dump mining stats in db 
 * TODO dump price stats in db
 *
 */
public class MiningDriver {
  public static void main(String args[]) {
    ProcessHandle.allProcesses().filter(h -> h.info().command().isPresent())
        .filter(h -> h.info().command().get().contains("bitcoin"))
        .forEach(handle -> System.out.println(handle.info()));
  }

  public void startMiner() throws IOException {
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    String minerCommand =
        "C:/Users/starf/Downloads/bitcoinmining/ccminer-818-cuda91-x64/ccminer.exe";
    Process handle = new ProcessBuilder(minerCommand).start();
  }

  public void stopMiner() {}
}


class Miner {
  Process start() {
    return null;
  }

  void stop() {};

  JsonNode status() {
    return null;
  };
}
