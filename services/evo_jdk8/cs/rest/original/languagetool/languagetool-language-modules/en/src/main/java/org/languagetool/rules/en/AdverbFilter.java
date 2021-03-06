/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.en;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.AbstractDateCheckFilter;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter that maps suggestion from adverb to adjective.
 * Also see https://www.ef.com/wwen/english-resources/english-grammar/forming-adverbs-adjectives/
 * @since 4.9
 */
public class AdverbFilter extends RuleFilter {

  private final Map<String,String> adverb2Adj = new HashMap<String, String>() {{
    // irregular ones:
    put("well", "good");
    put("fast", "fast");
    put("hard", "hard");
    put("late", "late");
    put("early", "early");
    put("daily", "daily");
    put("straight", "straight");
    // regular ones:
    put("simply", "simple");
    put("cheaply", "cheap");
    put("quickly", "quick");
    put("slowly", "slow");
    put("easily", "easy");
    put("angrily", "angry");
    put("happily", "happy");
    put("luckily", "lucky");
    put("terribly", "terrible");
    put("tragically", "tragic");
    put("economically", "economic");
    put("greatly", "great");
    put("highly", "high");
    put("generally", "general");
    put("differently", "different");
    put("rightly", "right");
    put("largely", "large");
    put("really", "real");
    put("philosophically", "philosophical");
    put("directly", "direct");
    put("clearly", "clear");
    put("merely", "mere");
    put("exactly", "exact");
    put("recently", "recent");
    put("rapidly", "rapid");
    put("suddenly", "sudden");
    put("extremely", "extreme");
    put("properly", "proper");
    put("politically", "political");
    put("probably", "probable");
    put("self-consciously", "self-conscious");
    put("successfully", "successful");
    put("unusually", "unusual");
    put("obviously", "obvious");
    put("currently", "current");
    put("residentially", "residential");
    put("fully", "full");
    put("accidentally", "accidental");
    put("medicinally", "medicinal");
    put("automatically", "automatic");
    put("completely", "complete");
    put("chronologically", "chronological");
    put("accurately", "accurate");
    put("necessarily", "necessary");
    put("temporarily", "temporary");
    put("significantly", "significant");
    put("hastily", "hasty");
    put("immediately", "immediate");
    put("rarely", "rare");
    put("totally", "total");
    put("literally", "literal");
    put("gently", "gentle");
    put("finally", "final");
    put("increasingly", "increasing");
    put("decreasingly", "decreasing");
    put("considerably", "considerable");
    put("effectively", "effective");
    put("fairly", "fair");
    put("briefly", "brief");
    put("exceedingly", "exceeding");
    put("physically", "physical");
    put("enthusiastically", "enthusiastic");
    put("incredibly", "incredible");
    put("permanently", "permanent");
    put("entirely", "entire");
    put("surely", "sure");
    put("positively", "positive");
    put("negatively", "negative");
    put("outwards", "outward");
    put("devastatingly", "devastating");
    put("relatively", "relative");
    put("absolutely", "absolute");
    put("socially", "social");
    put("consciously", "conscious");
    put("industriously", "industrious");
    put("solely", "sole");
    put("asynchronously", "asynchronous");
    put("fortunately", "fortunate");
    put("unfortunately", "unfortunate");
    put("ideally", "ideal");
    put("privately", "private");
    put("reasonably", "reasonable");
    put("unreasonably", "unreasonable");
    put("forcibly", "forcible");
    put("personally", "personal");
    put("basically", "basical");
    put("definitely", "definite");
    put("potentially", "potential");
    put("manually", "manual");
    put("continuously", "continuous");
    put("sadly", "sad");
    put("eventually", "eventual");
    put("possibly", "possible");
    put("visually", "visual");
    put("predominantly", "predominant");
    put("quietly", "quiet");
    put("slightly", "slight");
    put("cleverly", "clever");
    put("roughly", "rough");
    put("environmentally", "environmental");
    put("geographically", "geographical");
    put("usually", "usual");
    put("normally", "normal");
    put("deliciously", "delicious");
    put("steadily", "steady");
    put("actively", "active");
    put("schematically", "schematic");
    put("mindfully", "mindful");
    put("statistically", "statistical");
    put("culturally", "cultural");
    put("vicariously", "vicarious");
    put("vividly", "vivid");
    put("partially", "partial");
    put("unduly", "undue");
    put("seriously", "serious");
    put("non-verbally", "non-verbal");
    put("nonverbally", "nonverbal");
    put("verbally", "verbal");
    put("shortly", "short");
    put("mildly", "mild");
    put("secretly", "secret");
    put("especially", "especial");
    put("specially", "special");
    put("previously", "previous");
    put("whitely", "white");
    put("traditionally", "traditional");
    put("individually", "individual");
    put("carefully", "careful");
    put("essentially", "essential");
    put("originally", "original");
    put("alarmingly", "alarming");
    put("newly", "new");
    put("wrongfully", "wrongful");
    put("structurally", "structural");
    put("globally", "global");
    put("pacifically", "pacific");
    put("seemingly", "seeming");
    put("sustainably", "sustainable");
    put("momentarily", "momentary");
    put("coldly", "cold");
    put("nasally", "nasal");
    put("densely", "dense");
    put("grimly", "grim");
    put("calmly", "calm");
    put("racially", "racial");
    put("widely", "wide");
    put("peacefully", "peaceful");
    put("heavily", "heavy");
    put("authentically", "authentic");
    put("honestly", "honest");
    put("voluntarily", "voluntary");
    put("desperately", "desperate");
    put("immensely", "immense");
    put("apparently", "apparent");
    put("straightforwardly", "straightforward");
    put("anatomically", "anatomical");
    put("uniquely", "unique");
    put("systemically", "systemic");
    put("jokily", "jokey");
    put("critically", "critical");
    put("equally", "equal");
    put("strongly", "strong");
    put("purposely", "intentional");
    put("thoroughly", "thorough");
    put("outwardly", "outward");
    put("horizontally", "horizontal");
    put("vertically", "vertical");
    put("technically", "technical");
    put("swiftly", "swift");
    put("accessibly", "accessible");
    put("occasionally", "occasional");
    put("specifically", "specific");
    put("subtly", "subtle");
    put("actually", "actual");
    put("particularly", "particular");
    put("gloomily", "gloomy");
    put("nicely", "nice");
    put("progressively", "progressive");
    put("genuinely", "genuine");
    put("characteristically", "characteristic");
    put("deeply", "deep");
    put("spiritually", "spiritual");
    put("purely", "pure");
    put("satisfyingly", "satisfying");
    put("indolently", "indolent");
    put("obliquely", "oblique");
    put("preferably", "preferable");
    put("oddly", "odd");
    put("professionally", "professional");
    put("indispensably", "indispensable");
    put("dispensably", "dispensable");
    put("consistently", "consistent");
    put("truly", "true");
    put("commonly", "common");
    put("safely", "safe");
    put("evolutionarily", "evolutionary");
    put("internally", "internal");
    put("magically", "magical");
    put("annually", "annual");
    put("brightly", "bright");
    put("officially", "official");
    put("inofficially", "inofficial");
    put("perfectly", "perfect");
    put("overly", "over");
    put("tropically", "tropical");
    put("brilliantly", "brilliant");
    put("exclusively", "exclusive");
    put("commercially", "commercial");
    put("mischievously", "mischievous");
    put("weirdly", "weird");
    put("routinely", "routine");
    put("gruffly", "gruff");
    put("naturally", "natural");
    put("lightly", "light");
    put("haphazardly", "haphazard");
    put("lovingly", "loving");
    put("sagely", "sage");
    put("systematically", "systematical");
    put("academically", "academical");

    // TODO: add more or maybe use https://github.com/simplenlg/simplenlg?
    //put("", "");
  }};

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    String adverb = arguments.get("adverb");
    String noun = arguments.get("noun");
    String adjective = adverb2Adj.get(adverb);
    if (adjective != null && !adjective.equals(adverb)) {
      // we can't simply cut off "ly" because of cases like "simply" -> "simple" etc.
      match.setSuggestedReplacement(adjective + " " + noun);
    }
    return match;
  }
}
