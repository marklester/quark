package quark;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import quark.analysis.TradePairAnalysis;

public class TradePairsTest {
  @Test
  public void testTradePairManager() throws Exception {
    TradePairManager tpManager = TradePairManager.create();
    System.out.println(tpManager.getTradePairs());
  }
  
  @Test
  public void testTradePairAnalysis() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonTradePairs = mapper.readTree(new File("src/test/resources/tradepairs.json"));
    List<TradePair> tradePairs = StreamSupport.stream(jsonTradePairs.spliterator(), false).map(node -> new TradePair(node))
        .collect(Collectors.toList());
    TradePairAnalysis analysis = new TradePairAnalysis();
    analysis.analyze(tradePairs);
  }
}
