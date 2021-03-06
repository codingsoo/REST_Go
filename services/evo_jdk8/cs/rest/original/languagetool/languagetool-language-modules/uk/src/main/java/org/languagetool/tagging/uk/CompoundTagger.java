/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.uk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.rules.uk.ExtraDictionaryLoader;
import org.languagetool.rules.uk.LemmaHelper;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tools.StringTools;

/**
 * Allows to tag compound words with hyphen dynamically by analyzing each part
 * 
 * @since 3.0
 */
class CompoundTagger {
  private static final String TAG_ANIM = ":anim";
  private static final String TAG_INANIM = ":inanim";
  private static final Pattern EXTRA_TAGS = Pattern.compile(":bad");
  private static final Pattern EXTRA_TAGS_DROP = Pattern.compile(":(comp.|np|ns|slang|rare|xp[1-9]|&predic|&insert)");
  private static final Pattern NOUN_SING_V_ROD_REGEX = Pattern.compile("noun.*?:[mfn]:v_rod.*");
//  private static final Pattern NOUN_V_NAZ_REGEX = Pattern.compile("noun.*?:.:v_naz.*");
  private static final Pattern SING_REGEX_F = Pattern.compile(":[mfn]:");
  private static final Pattern O_ADJ_PATTERN = Pattern.compile(".+?(??|[??????]??)");
  private static final Pattern NUMR_ADJ_PATTERN = Pattern.compile(".+?(????????|??????|????|??)");
  private static final Pattern DASH_PREFIX_LAT_PATTERN = Pattern.compile("[a-zA-Z]{3,}|[??-????-??]");
  private static final Pattern YEAR_NUMBER = Pattern.compile("[12][0-9]{3}");
  private static final Pattern NOUN_PREFIX_NUMBER = Pattern.compile("[0-9]+");
  private static final Pattern NOUN_SUFFIX_NUMBER_LETTER = Pattern.compile("[0-9][0-9??-??????????-]*");
  private static final Pattern ADJ_PREFIX_NUMBER = Pattern.compile("[0-9]+(,[0-9]+)?([-??????][0-9]+(,[0-9]+)?)?%?|(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})|??{2,3}");
  private static final Pattern REQ_NUM_DVA_PATTERN = Pattern.compile("(????????|????????????|????????????????).{0,4}");
  private static final Pattern REQ_NUM_DESYAT_PATTERN = Pattern.compile("(????????????[????]|??????????????|????????|??????????|????????????|??????????????????|????????????????????|??????).{0,4}");
  private static final Pattern REQ_NUM_STO_PATTERN = Pattern.compile("(????????|????????|????????????|????????????|????????????????).{0,3}");
  private static final Pattern INTJ_PATTERN = Pattern.compile("intj.*");
  private static final Pattern ONOMAT_PATTERN = Pattern.compile("onomat.*");
  private static final Pattern UKR_LETTERS_PATTERN = Pattern.compile("[??-????????????-??????????'-]+");

  private static final Pattern MNP_NAZ_REGEX = Pattern.compile(".*?:[mnp]:v_naz.*");
  private static final Pattern MNP_ZNA_REGEX = Pattern.compile(".*?:[mnp]:v_zna.*");
  private static final Pattern MNP_ROD_REGEX = Pattern.compile(".*?:[mnp]:v_rod.*");

  private static final Pattern stdNounTagRegex = Pattern.compile("noun:(?:in)?anim:(.):(v_...).*");
  private static final Map<String, String> dashPrefixes;
  private static final Set<String> leftMasterSet;
  private static final Map<String, List<String>> numberedEntities;
  private static final Map<String, Pattern> rightPartsWithLeftTagMap = new HashMap<>();
  private static final Set<String> slaveSet;
  private static final Set<String> dashPrefixesInvalid;
  private static final String ADJ_TAG_FOR_PO_ADV_MIS = "adj:m:v_mis";
  private static final String ADJ_TAG_FOR_PO_ADV_NAZ = "adj:m:v_naz";
  private static final Pattern PREFIX_NO_DASH_POSTAG_PATTERN = Pattern.compile("(noun|adj|adv)(?!.*&pron).*");

  // ?????????????????? ?????????????????? ?????????????????????????? ???????? ??????????????, ???? ???? ?????????? ???????????????????????? ??????????????????????
  private static final List<String> LEFT_O_ADJ = Arrays.asList(
    "????????????", "??????????", "??????????????????", "??????????", "????????", "??????", "????????????", "??????????", "????????????????", "??????????", "????????????????", "????????", "??????????", "????????????"
  );

  private static final List<String> LEFT_O_ADJ_INVALID = Arrays.asList(
    "????????????", "????????", "????????????", "????????????", "??????????", "????????"
  );

  // TODO: ?????????????????? ??????????-2014, ???????????? ????????-2018, ?????????????? ????????-2011, ???????? ??????????-2012, ?????????????? ??????????-2
  private static final List<String> WORDS_WITH_YEAR = Arrays.asList(
      "????????????", "????????????", "??????", "????????????????????", "????????????????", "????????????????",
      "??????????????????", "????????????????????????????????", "????????????????????", "??????????????????",
      "??????????????", "??????????????????????????", "????????????????", "??'????", "??????????????????", "????????????????????", 
      "??????????????????", "????????????????", "????????????????", "????????????????????????", "????????????", "??????????????", "??????????????", "??????????", 
      "????????????", "??????????????????????", "??????????????????", "??????????", "??????????????????", "??????????????", "??????????????????", "??????????????");
  private static final List<String> WORDS_WITH_NUM = Arrays.asList("??????????????", "??????????????", "????????????", "????????????", "????????????????", 
      "??????????", "????????????????", "??????????????", "????????????????", "????????", "??????????"); //TODO: ??????????-2 - prop
  private static final Pattern SKY_PATTERN = Pattern.compile(".*[??????]??????");
  private static final Pattern SKYI_PATTERN = Pattern.compile(".*[??????]????????");

  // http://www.pravopys.net/sections/33/
  static {
    rightPartsWithLeftTagMap.put("????", Pattern.compile("(verb|.*?pron|noun|adv|intj|part).*"));
    rightPartsWithLeftTagMap.put("????", Pattern.compile("(verb.*?:(impr|futr|&insert)|intj).*")); 
    rightPartsWithLeftTagMap.put("????", Pattern.compile("(.*?pron|adv|part|verb).*"));
    rightPartsWithLeftTagMap.put("????", Pattern.compile("(.*?pron|verb|noun|adj|conj).*")); // adv|part|conj
    // noun gives false on ??????????-????????
    rightPartsWithLeftTagMap.put("????????", Pattern.compile("(verb|adv|adj|.*?pron|part|noninfl:&predic).*")); 

    dashPrefixes = ExtraDictionaryLoader.loadMap("/uk/dash_prefixes.txt");
    dashPrefixesInvalid = ExtraDictionaryLoader.loadSet("/uk/dash_prefixes_invalid.txt");
    leftMasterSet = ExtraDictionaryLoader.loadSet("/uk/dash_left_master.txt");
    // TODO: "????????????", "????????????", "????????????????" - not quite slaves, could be masters too
    slaveSet = ExtraDictionaryLoader.loadSet("/uk/dash_slaves.txt");
    numberedEntities = ExtraDictionaryLoader.loadSpacedLists("/uk/entities.txt");
  }

  private final WordTagger wordTagger;
  private final Locale conversionLocale;
  private final UkrainianTagger ukrainianTagger;
  private final CompoundDebugLogger compoundDebugLogger = new CompoundDebugLogger();


  CompoundTagger(UkrainianTagger ukrainianTagger, WordTagger wordTagger, Locale conversionLocale) {
    this.ukrainianTagger = ukrainianTagger;
    this.wordTagger = wordTagger;
    this.conversionLocale = conversionLocale;
  }


  @Nullable
  public List<AnalyzedToken> guessCompoundTag(String word) {
    List<AnalyzedToken> guessedTokens = doGuessCompoundTag(word);
    compoundDebugLogger.logTaggedCompound(guessedTokens);
    return guessedTokens;
  }

  @Nullable
  private List<AnalyzedToken> doGuessCompoundTag(String word) {
    int dashIdx = word.lastIndexOf('-');
    if( dashIdx == word.length() - 1 )
      return null;

    int firstDashIdx = word.indexOf('-');
    if( firstDashIdx == 0 )
      return null;

    boolean startsWithDigit = Character.isDigit(word.charAt(0));

    if( ! startsWithDigit && dashIdx != firstDashIdx ) {
      int dashCount = StringUtils.countMatches(word, "-");

      if( dashCount >= 2
          && dashIdx > firstDashIdx + 1 ) {
        List<AnalyzedToken> tokens = doGuessMultiHyphens(word, firstDashIdx, dashIdx);
        if( tokens != null )
          return tokens;
      }
      
      if( dashCount == 2
          && dashIdx > firstDashIdx + 1 ) {
        return doGuessTwoHyphens(word, firstDashIdx, dashIdx);
      }
      
      return null;
    }

    String leftWord = word.substring(0, dashIdx);
    String rightWord = word.substring(dashIdx + 1);

    // ??-??????????????????????
    if( leftWord.length() == 1 && rightWord.length() > 3 && rightWord.startsWith(leftWord.toLowerCase()) ) {
      List<TaggedWord> rightWdList = wordTagger.tag(rightWord);
      rightWdList = PosTagHelper.adjust(rightWdList, ":alt", null);
      return ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, rightWdList);
    }
    
    
    boolean dashPrefixMatch = dashPrefixes.containsKey( leftWord ) 
        || dashPrefixes.containsKey( leftWord.toLowerCase() ) 
        || DASH_PREFIX_LAT_PATTERN.matcher(leftWord).matches();

    if( ! dashPrefixMatch 
        && (startsWithDigit || word.matches("[XLIV]+-.*")) ) {
      return matchDigitCompound(word, leftWord, rightWord);
    }

    if( Character.isDigit(rightWord.charAt(0)) ) {
      return matchNumberedProperNoun(word, leftWord, rightWord);
    }


    // ????????..., ????????... ???????????????? ??????????
    //TODO: ?????? ???????? ????????: ????????-??????????????????
    if( dashPrefixesInvalid.contains(leftWord.toLowerCase()) ) {
      List<TaggedWord> rightWdList = tagEitherCase(rightWord);
      
      rightWdList = PosTagHelper.filter2(rightWdList, Pattern.compile("(noun|adj)(?!.*pron).*"));
      
      if( rightWdList.isEmpty() )
        return null;

//      String lemma = leftWord + "-" + rightWdList.get(0).getLemma();
      String extraTag = StringTools.isCapitalizedWord(rightWord) ? "" : ":bad";
      rightWdList = PosTagHelper.adjust(rightWdList, extraTag, leftWord + "-");
      return ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, rightWdList);
    }


    // wrong: ??????-????????????
    if( leftWord.equalsIgnoreCase("??????")
        && Character.isLowerCase(rightWord.charAt(0)) )
      return null;

    List<TaggedWord> leftWdList = tagAsIsAndWithLowerCase(leftWord);


    // ??????????????-????, ??????????-????, ??????????????-????????, ??????????-????, ??????????-????

    if( rightPartsWithLeftTagMap.containsKey(rightWord) 
        && ! PosTagHelper.hasPosTagPart2(leftWdList, "abbr") ) {

      if( leftWdList.isEmpty() )
        return null;

      Pattern leftTagRegex = rightPartsWithLeftTagMap.get(rightWord);

      List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(leftWord, leftWdList);
      List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(leftAnalyzedTokens.size());

      // ignore ??????-????
      if( rightWord.equals("????")
          && LemmaHelper.hasLemma(leftAnalyzedTokens, Arrays.asList("??????", "????", "????")) )
        return null;

      for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {
        String posTag = analyzedToken.getPOSTag();
        if (leftWord.equalsIgnoreCase("????") && posTag != null && posTag.contains("noun") )
          continue;
          
        if( posTag != null
            && (leftWord.equals("????????") && posTag.contains("adv")) 
             || (leftTagRegex.matcher(posTag).matches()) ) {
          
          newAnalyzedTokens.add(new AnalyzedToken(word, posTag, analyzedToken.getLemma()));
        }
      }

      return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
    }


    // ????-????????????????????, ????-????????????????????????

    if( leftWord.equalsIgnoreCase("????") && SKY_PATTERN.matcher(rightWord).matches() ) {
      rightWord += "??";
    }
    
    // ????????????????????????-??????????

    if( Character.isUpperCase(leftWord.charAt(0)) && LemmaHelper.CITY_AVENU.contains(rightWord) ) {
      String addPos = rightWord.equals("??????????????") ? ":alt" : "";
      return PosTagHelper.generateTokensForNv(word, "f", ":prop" + addPos);
    }

    // Fe-??????????????
    if( rightWord.startsWith("??????????") ) {
      String adjustedWord = "????????" + rightWord;
      List<TaggedWord> rightWdList = tagEitherCase(adjustedWord);
      rightWdList = rightWdList.stream().map(wd -> new TaggedWord("??????????????", wd.getPosTag())).collect(Collectors.toList());
      List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList);
      return generateTokensWithRighInflected(word, leftWord, rightAnalyzedTokens, IPOSTag.adj.getText(), null);
    }

    List<TaggedWord> rightWdList = tagEitherCase(rightWord);
      
     
    if( word.startsWith("??????????") ) {
      // ????????????????????????????-????????????????????????????????
      Matcher napivMatcher = Pattern.compile("??????????(.+?)-??????????(.+)").matcher(word);
      if( napivMatcher.matches() ) {
        List<TaggedWord> napivLeftWdList = PosTagHelper.adjust(tagAsIsAndWithLowerCase(napivMatcher.group(1)), null, "??????????");
        List<TaggedWord> napivRightWdList = rightWdList.size() > 0 ? rightWdList : PosTagHelper.adjust(tagAsIsAndWithLowerCase(napivMatcher.group(2)), null, "??????????");

        if( napivLeftWdList.isEmpty() || napivRightWdList.isEmpty() )
          return null;
        
        List<AnalyzedToken> napivLeftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(napivMatcher.group(1), napivLeftWdList);
        List<AnalyzedToken> napivRightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(napivMatcher.group(2), napivRightWdList);

        List<AnalyzedToken> tagMatch = tagMatch(word, napivLeftAnalyzedTokens, napivRightAnalyzedTokens);
        if( tagMatch != null ) {
          return tagMatch;
        }
      }
    }
      
    if( rightWdList.isEmpty() ) {
      return null;
    }

    List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList);

    // ??-????????????????
    if( leftWord.length() == 1
        && Character.isUpperCase(leftWord.charAt(0))
        && LemmaHelper.hasLemma(rightAnalyzedTokens, Arrays.asList("????????????????")) ) {

      return generateTokensWithRighInflected(word, leftWord, rightAnalyzedTokens, IPOSTag.adj.getText(), null);
    }

    if( leftWord.equalsIgnoreCase("????") ) {
      if( rightWord.endsWith("??????") ) {
        return poAdvMatch(word, rightAnalyzedTokens, ADJ_TAG_FOR_PO_ADV_MIS);
      }
      else if( SKYI_PATTERN.matcher(rightWord).matches() ) {
        return poAdvMatch(word, rightAnalyzedTokens, ADJ_TAG_FOR_PO_ADV_NAZ);
      }
      return null;
    }


    // exclude: ??????????????-????, ??????????????????-????????

    List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(leftWord, leftWdList);
    
    if( PosTagHelper.hasPosTagPart(leftAnalyzedTokens, "&pron")
        && ! PosTagHelper.hasPosTagPart(leftAnalyzedTokens, "numr") )
      return null;

    if( ! leftWord.equalsIgnoreCase(rightWord) && PosTagHelper.hasPosTag(rightAnalyzedTokens, Pattern.compile("(part|conj).*|.*?:&pron.*")) 
        && ! (PosTagHelper.hasPosTagStart(leftAnalyzedTokens, "numr") && PosTagHelper.hasPosTagStart(rightAnalyzedTokens, "numr")) )
      return null;


    // ??????????????-??????????
    
    if( dashPrefixMatch 
        && ! ( leftWord.equalsIgnoreCase("????????") && LemmaHelper.hasLemma(rightAnalyzedTokens, Arrays.asList("????????????"))) ) {
      List<AnalyzedToken> newTokens = new ArrayList<>();
      if( leftWord.length() == 1 && leftWord.matches("[a-zA-Z??-????-??]") ) {
        List<AnalyzedToken> newTokensAdj = getNvPrefixLatWithAdjMatch(word, rightAnalyzedTokens, leftWord);
        if( newTokensAdj != null ) {
          newTokens.addAll(newTokensAdj);
        }
      }
      
      String extraTag = "";
      if( dashPrefixes.containsKey( leftWord ) ) {
        extraTag = dashPrefixes.get(leftWord);
      }
      else { 
        if( dashPrefixes.containsKey( leftWord.toLowerCase() ) ) {
          extraTag = dashPrefixes.get(leftWord.toLowerCase());
        }
      }
      
      List<AnalyzedToken> newTokensNoun = getNvPrefixNounMatch(word, rightAnalyzedTokens, leftWord, extraTag);
      if( newTokensNoun != null ) {
        newTokens.addAll(newTokensNoun);
      }
      
      // ??????-????????????
      if( leftWord.equalsIgnoreCase("??????") && PosTagHelper.hasPosTagPart(rightAnalyzedTokens, "numr:") ) {
        return generateTokensWithRighInflected(word, leftWord, rightAnalyzedTokens, "numr:", ":bad");
      }
      
      return newTokens;
    }

    // ??????-??????????????

    if( Character.isUpperCase(rightWord.charAt(0)) ) {
      if (word.startsWith("??????-")) {
        List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(rightAnalyzedTokens.size());
        
        for (AnalyzedToken rightAnalyzedToken : rightAnalyzedTokens) {
          String rightPosTag = rightAnalyzedToken.getPOSTag();

          if( rightPosTag == null )
            continue;

          if( NOUN_SING_V_ROD_REGEX.matcher(rightPosTag).matches() ) {
            for(String vid: PosTagHelper.VIDMINKY_MAP.keySet()) {
              if( vid.equals("v_kly") )
                continue;
              String posTag = rightPosTag.replace("v_rod", vid) + ":ua_1992";
              newAnalyzedTokens.add(new AnalyzedToken(word, posTag, word));
            }
          }
        }

        return newAnalyzedTokens;
      }
      else {
        // we don't want ??????-?????????? but want ??????????????????????-??????????????????????
        if( StringTools.isCapitalizedWord(rightWord)
            || leftWord.endsWith("??")
            || PosTagHelper.hasPosTag(rightAnalyzedTokens, Pattern.compile("adj.*")) ) {

          // tag ????????????????????????/noun ?? ???????????????????????? adj
          List<TaggedWord> rightWdList2 = tagAsIsAndWithLowerCase(rightWord);
          List<AnalyzedToken> rightAnalyzedTokens2 = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList2);

          List<AnalyzedToken> match = tryOWithAdj(word, leftWord, rightAnalyzedTokens2);
          if( match != null )
            return match;
        }

        return null;
      }
    }

    // don't allow: ????????????-??????, ??????????????????-??????????????????????, ??????-??????????

    // allow ????-????!

    boolean hasIntj = PosTagHelper.hasPosTagStart(leftAnalyzedTokens, "intj");
    if( ! hasIntj ) {
      String noDashWord = word.replace("-", "");
      List<TaggedWord> noDashWordList = tagAsIsAndWithLowerCase(noDashWord);
      List<AnalyzedToken> noDashAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(noDashWord, noDashWordList);

      if( ! noDashAnalyzedTokens.isEmpty() )
        return null;
    }


    // ??????????-????????, ??????????-????????????????, ????????-????????

    if( ! leftWdList.isEmpty() && (leftWord.length() > 2 || hasIntj) ) {
      List<AnalyzedToken> tagMatch = tagMatch(word, leftAnalyzedTokens, rightAnalyzedTokens);
      if( tagMatch != null ) {
        return tagMatch;
      }
    }

    List<AnalyzedToken> match = tryOWithAdj(word, leftWord, rightAnalyzedTokens);
    if( match != null )
      return match;

    compoundDebugLogger.logUnknownCompound(word);
    
    return null;
  }

  private List<TaggedWord> tagEitherCase(String word) {
    if( word.isEmpty() )
      return new ArrayList<>();
    
    List<TaggedWord> rightWdList = wordTagger.tag(word);
    if( rightWdList.isEmpty() ) {
      if( Character.isUpperCase(word.charAt(0)) ) {
        rightWdList = wordTagger.tag(word.toLowerCase());
      }
    }
    return rightWdList;
  }

//  private List<TaggedWord> tagBothCases(String rightWord) {
//    List<TaggedWord> rightWdList = wordTagger.tag(rightWord);
//    if( Character.isUpperCase(rightWord.charAt(0)) ) {
//      rightWdList = wordTagger.tag(rightWord.toLowerCase());
//    }
//    return rightWdList;
//  }


  private List<AnalyzedToken> tryOWithAdj(String word, String leftWord, List<AnalyzedToken> rightAnalyzedTokens) {
    if( leftWord.length() < 3 )
      return null;
    
    // ????????????..., ????????.... ???????????????? ??????????
    if( LEFT_O_ADJ_INVALID.contains(leftWord.toLowerCase()) )
      return null;

    // ??????-??????????????????????...
    if( NUMR_ADJ_PATTERN.matcher(leftWord).matches() ) {
      return numrAdjMatch(word, rightAnalyzedTokens, leftWord);
    }

    // ??????????????-??????????????????...
    if( O_ADJ_PATTERN.matcher(leftWord).matches() ) {
      return oAdjMatch(word, rightAnalyzedTokens, leftWord);
    }
    
    return null;
  }

  private List<AnalyzedToken> doGuessMultiHyphens(String word, int firstDashIdx, int dashIdx) {
    String lowerWord = word.toLowerCase();

    String[] parts = lowerWord.split("-");
    LinkedHashSet<String> set = new LinkedHashSet<>(Arrays.asList(parts));
    // try intj
    String leftWd = parts[0];
    if( set.size() == 2 ) {
      List<TaggedWord> leftWdList = tagEitherCase(leftWd);
      List<TaggedWord> rightWdList = tagEitherCase(new ArrayList<>(set).get(1));

      if( PosTagHelper.hasPosTag2(leftWdList, INTJ_PATTERN)
          && PosTagHelper.hasPosTag2(rightWdList, INTJ_PATTERN)
          || PosTagHelper.hasPosTag2(leftWdList, ONOMAT_PATTERN)
          && PosTagHelper.hasPosTag2(rightWdList, ONOMAT_PATTERN)) {
        return Arrays.asList(new AnalyzedToken(word, rightWdList.get(0).getPosTag(), lowerWord));
      }
    }
    else if( set.size() == 1 ) {
      if( lowerWord.equals("????") ) {
        return Arrays.asList(new AnalyzedToken(word, "intj", lowerWord));
      }

      List<TaggedWord> rightWdList = tagEitherCase(leftWd);
      if( PosTagHelper.hasPosTag2(rightWdList, INTJ_PATTERN) ) {
        return Arrays.asList(new AnalyzedToken(word, rightWdList.get(0).getPosTag(), lowerWord));
      }
    }

    // filter out ??-??-??
    if( parts.length >= 3 && set.size() > 1
        && ! dashPrefixes.containsKey(parts[0])
        && ! dashPrefixesInvalid.contains(parts[0]) ) {

      // ????-????-????-????
      String merged = word.replace("-", "");
      List<TaggedWord> tagged = tagBothCases(merged,  null);
      if( ! tagged.isEmpty() ) {
        return ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, PosTagHelper.addIfNotContains(tagged, ":alt"));
      }

      // ????-??-??-????
      merged = collapseStretch(word);
      tagged = tagBothCases(merged, null);
      if( ! tagged.isEmpty() ) {
        return ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, PosTagHelper.addIfNotContains(tagged, ":alt"));
      }
    }

    return null;
  }

  private final static Pattern STRETCH_PATTERN = Pattern.compile("([??-????????????-??????????])\\1*-\\1+");
  
  private static String collapseStretch(String word) {
    boolean capitalized = StringTools.isCapitalizedWord(word);
    String merged = STRETCH_PATTERN.matcher(word.toLowerCase()).replaceAll("$1");
    merged = STRETCH_PATTERN.matcher(merged).replaceAll("$1");
    merged = merged.replace("-", "");
    if( capitalized ) {
      merged = StringUtils.capitalize(merged);
    }
    return merged;
  }


  private List<AnalyzedToken> doGuessTwoHyphens(String word, int firstDashIdx, int dashIdx) {
    String[] parts = word.split("-");

    List<TaggedWord> rightWdList = tagEitherCase(parts[2]);

    if( rightWdList.isEmpty() )
      return null;

    List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(parts[2], rightWdList);

    String firstAndSecond = parts[0] + "-" + parts[1];

    boolean twoDash = false;
    String extraTag = "";
    if( dashPrefixes.containsKey(firstAndSecond) ) {
      extraTag = dashPrefixes.get(firstAndSecond);
      twoDash = true;
    }
    else if( dashPrefixes.containsKey( firstAndSecond.toLowerCase() ) ) {
      extraTag = dashPrefixes.get(firstAndSecond.toLowerCase());
      twoDash = true;
    }

    if( twoDash ) {
      return getNvPrefixNounMatch(word, rightAnalyzedTokens, firstAndSecond, extraTag);
    }


    List<TaggedWord> secondWdList = tagEitherCase(parts[1]);
    
    // try full match - only adj for now - nouns are complicated
    if( PosTagHelper.hasPosTagStart2(secondWdList, "adj") ) {
      List<AnalyzedToken> secondAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(parts[1], secondWdList);

      List<AnalyzedToken> tagMatchSecondAndThird = tagMatch(word, secondAnalyzedTokens, rightAnalyzedTokens);
      if( tagMatchSecondAndThird != null ) {
        
        List<TaggedWord> leftWdList = tagEitherCase(parts[0]);
        
        if( ! leftWdList.isEmpty() ) {
          List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(parts[0], leftWdList);
          tagMatch(word, leftAnalyzedTokens, tagMatchSecondAndThird);
        }
        
        return tagMatchSecondAndThird;
      }
    }

    // try ????????????????-??????????????????????-????????????????????????????
    List<AnalyzedToken> secondAndThird = tryOWithAdj(word, parts[1], rightAnalyzedTokens);
    
    if( secondAndThird != null ) {
      return tryOWithAdj(word, parts[0], secondAndThird);
    }
    
    return null;
  }


  private static List<AnalyzedToken> generateTokensWithRighInflected(String word, String leftWord, List<AnalyzedToken> rightAnalyzedTokens, String posTagStart, String addTag) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(rightAnalyzedTokens.size());
    for (AnalyzedToken analyzedToken : rightAnalyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( posTagStart )
            && ! posTag.contains("v_kly") ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, PosTagHelper.addIfNotContains(posTag, addTag), leftWord + "-" + analyzedToken.getLemma()));
      }
    }
    return newAnalyzedTokens;
  }


  private List<AnalyzedToken> matchNumberedProperNoun(String word, String leftWord, String rightWord) {

    // ????-140
    if( NOUN_SUFFIX_NUMBER_LETTER.matcher(rightWord).matches() ) {
      Set<AnalyzedToken> newAnalyzedTokens = new LinkedHashSet<>();

      for(Map.Entry<String, List<String>> entry: numberedEntities.entrySet()) {
        if( word.matches(entry.getKey()) ) {
            for(String tag: entry.getValue()) {
                if( tag.contains(":nv") ) {
                  String[] tagParts = tag.split(":");
                  String extraTags = tag.replaceFirst(".*?:nv", "").replace(":np", "");
                  List<AnalyzedToken> newTokens = PosTagHelper.generateTokensForNv(word, tagParts[1], extraTags);
                  newAnalyzedTokens.addAll(newTokens);

                  if( ! tag.contains(":np") && ! tag.contains(":p") ) {
                    newTokens = PosTagHelper.generateTokensForNv(word, "p", extraTags);
                    newAnalyzedTokens.addAll(newTokens);
                  }
                }
                else {
                    newAnalyzedTokens.add(new AnalyzedToken(word, tag, word));
                }
            }
        }
      }

      if (newAnalyzedTokens.size() > 0)
        return new ArrayList<>(newAnalyzedTokens);
    }

    // ????????????-2014
    if (YEAR_NUMBER.matcher(rightWord).matches()) {
      List<TaggedWord> leftWdList = tagAsIsAndWithLowerCase(leftWord);

      if (!leftWdList.isEmpty() /*&& Character.isUpperCase(leftWord.charAt(0))*/) {
        List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(leftWord, leftWdList);

        List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();

        boolean isUppercase = Character.isUpperCase(leftWord.charAt(0));
        for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {
          if (!PosTagHelper.hasPosTagPart(analyzedToken, ":prop")
              && !WORDS_WITH_YEAR.contains(analyzedToken.getLemma()))
            continue;

          String posTag = analyzedToken.getPOSTag();

          // only noun - ????????????????????: ???????????? - ????????????????
          // ??????????-2014 - ???????????????? ???????? ??????????, ???? ????'??
          // TODO: ??????????????-2012
          if (posTag == null || ! posTag.startsWith("noun:inanim"))
            continue;

          if (posTag.contains("v_kly"))
            continue;

          if (posTag.contains(":p:") 
              && !Arrays.asList("??????", "????????????").contains(analyzedToken.getLemma())
              && !posTag.contains(":ns"))
            continue;

          String lemma = analyzedToken.getLemma();
          posTag = posTag.replace(":geo", "");

          if (!posTag.contains(":prop")) {
            if( isUppercase ) {
              posTag += ":prop";
              lemma = StringUtils.capitalize(lemma);
            }
          }
          newAnalyzedTokens.add(new AnalyzedToken(word, posTag, lemma + "-" + rightWord));

        }

        if (newAnalyzedTokens.size() > 0)
          return newAnalyzedTokens;
      }
    }


    // ??????????????-1, ??????????????-2, ??????????-3
    if (NOUN_PREFIX_NUMBER.matcher(rightWord).matches()) {
      List<TaggedWord> leftWdList = tagAsIsAndWithLowerCase(leftWord);

      if (!leftWdList.isEmpty() /*&& Character.isUpperCase(leftWord.charAt(0)) && leftWord.matches("[??-??????????][??-??????????'].*")*/ ) {

        List<AnalyzedToken> leftAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(leftWord, leftWdList);
        List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();

        for (AnalyzedToken analyzedToken : leftAnalyzedTokens) {

          String posTag = analyzedToken.getPOSTag();
          String lemma = analyzedToken.getLemma();

          if( posTag == null || ! posTag.startsWith("noun:inanim") )
            continue;

          if (posTag.contains("v_kly"))
            continue;

          if ( ! posTag.contains(":prop") ) {
            if( ! WORDS_WITH_NUM.contains(lemma) ) {
              posTag += ":prop";
              lemma = StringUtils.capitalize(lemma);
            }
          }

          if( ! WORDS_WITH_NUM.contains(lemma) )
            continue;


          newAnalyzedTokens.add(new AnalyzedToken(word, posTag, lemma + "-" + rightWord));
        }

        if (newAnalyzedTokens.size() > 0)
          return newAnalyzedTokens;
      }
    }

    return null;
  }


  private List<AnalyzedToken> matchDigitCompound(String word, String leftWord, String rightWord) {
    // 101-??, 100-??????????????

    if( ADJ_PREFIX_NUMBER.matcher(leftWord).matches() ) {
      List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();

      // e.g. 101-????
      String[] tags = LetterEndingForNumericHelper.findTags(leftWord, rightWord); 
      if( tags != null ) {
        for (String tag: tags) {
          String lemma = leftWord + "-" + "??";  // lemma is approximate here, we mostly care about the tag
          newAnalyzedTokens.add(new AnalyzedToken(word, IPOSTag.adj.getText() + tag + ":&numr", lemma));
        }

        // ?? 3-???? ?????????????? - ???? ???????? ??????????????????, ?????? ???????????????? ??????????
        if( "????".equals(rightWord) ) {
          newAnalyzedTokens.add(new AnalyzedToken(word, IPOSTag.noun.getText() + ":p:v_oru:&numr:bad", leftWord));
        }
        // ???????????????? 148-???? ??????????
        else if( "????".equals(rightWord) 
            && Pattern.compile("(.*[^1]|^)[78]").matcher(leftWord).matches() ) {
          newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_rod:bad", leftWord));
          newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_dav:bad", leftWord));
          newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_mis:bad", leftWord));
        }
      }
      else {
        if( NOUN_PREFIX_NUMBER.matcher(leftWord).matches() ) {
          // 100-??????????
          
          String tryPrefix = getTryPrefix(rightWord);
          
          if( tryPrefix != null ) {
            List<TaggedWord> rightWdList = wordTagger.tag(tryPrefix + rightWord);
            
            if( rightWdList == null )
              return null;

            List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList);

            for (AnalyzedToken analyzedToken : rightAnalyzedTokens) {
              String lemma = analyzedToken.getLemma().substring(tryPrefix.length());
              newAnalyzedTokens.add(new AnalyzedToken(word, analyzedToken.getPOSTag(), leftWord + "-" + lemma));
            }

            return newAnalyzedTokens;
          }
          // 100-???? ??????????????
          else if( "????".equals(rightWord) ) {
            for(String gender: PosTagHelper.BASE_GENDERS ) {
              for(String vidm: PosTagHelper.VIDMINKY_MAP.keySet()) {
                if( vidm.equals("v_kly") )
                  continue;

                String posTag = IPOSTag.adj.getText() + ":" + gender + ":" + vidm;
                newAnalyzedTokens.add(new AnalyzedToken(word, posTag, word));
              }
            }
            return newAnalyzedTokens;
          }
          // ???????????????? 15-???? ??????????
          else if( "????".equals(rightWord) 
              && Pattern.compile(".*([0569]|1[0-9])").matcher(leftWord).matches() ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_rod:bad", leftWord));
            newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_dav:bad", leftWord));
            newAnalyzedTokens.add(new AnalyzedToken(word, "numr:p:v_mis:bad", leftWord));
          }
        }

        // e.g. 100-??????????????, 100-????????????????????
        List<TaggedWord> rightWdList = wordTagger.tag(rightWord);
        if( rightWdList.isEmpty() )
          return null;

        List<AnalyzedToken> rightAnalyzedTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(rightWord, rightWdList);

        for (AnalyzedToken analyzedToken : rightAnalyzedTokens) {
          if( analyzedToken.getPOSTag().startsWith(IPOSTag.adj.getText())
              || "????????????????????".equals(analyzedToken.getLemma()) ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, analyzedToken.getPOSTag(), leftWord + "-" + analyzedToken.getLemma()));
          }
        }
      }
      return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
    }
    
    return null;
  }


  private String getTryPrefix(String rightWord) {
    if( REQ_NUM_STO_PATTERN.matcher(rightWord).matches() )
      return "??????";
    if( REQ_NUM_DESYAT_PATTERN.matcher(rightWord).matches() ) 
      return "????????????";
    if( REQ_NUM_DVA_PATTERN.matcher(rightWord).matches() ) 
      return "??????";

    return null;
  }


  @Nullable
  private List<AnalyzedToken> tagMatch(String word, List<AnalyzedToken> leftAnalyzedTokens, List<AnalyzedToken> rightAnalyzedTokens) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>();
    List<AnalyzedToken> newAnalyzedTokensAnimInanim = new ArrayList<>();

    String animInanimNotTagged = null;

    for (AnalyzedToken leftAnalyzedToken : leftAnalyzedTokens) {
      String leftPosTag = leftAnalyzedToken.getPOSTag();

      if( leftPosTag == null 
          || IPOSTag.contains(leftPosTag, IPOSTag.abbr.getText()) )
        continue;

      // we don't want to have v_kly for ????????-??????????????????
      // but we do for ????????-????????????????
      if( leftPosTag.startsWith("noun:inanim")
          && leftPosTag.contains("v_kly") )
        continue;

      String leftPosTagExtra = "";
      boolean leftNv = false;

      if( leftPosTag.contains(PosTagHelper.NO_VIDMINOK_SUBSTR) ) {
        leftNv = true;
        leftPosTag = leftPosTag.replace(PosTagHelper.NO_VIDMINOK_SUBSTR, "");
      }

      Matcher matcher = EXTRA_TAGS_DROP.matcher(leftPosTag);
      if( matcher.find() ) {
        leftPosTag = matcher.replaceAll("");
      }

      matcher = EXTRA_TAGS.matcher(leftPosTag);
      if( matcher.find() ) {
        leftPosTagExtra += matcher.group();
        leftPosTag = matcher.replaceAll("");
      }

      for (AnalyzedToken rightAnalyzedToken : rightAnalyzedTokens) {
        String rightPosTag = rightAnalyzedToken.getPOSTag();

        if( rightPosTag == null
//            || rightPosTag.contains("v_kly")
            || rightPosTag.contains(IPOSTag.abbr.getText()) )
          continue;

        if( rightPosTag.startsWith("noun:inanim")
            && rightPosTag.contains("v_kly") )
          continue;

        String extraNvTag = "";
        boolean rightNv = false;
        if( rightPosTag.contains(PosTagHelper.NO_VIDMINOK_SUBSTR) ) {
          rightNv = true;
          
          if( leftNv ) {
            extraNvTag += PosTagHelper.NO_VIDMINOK_SUBSTR;
          }
        }

        Matcher matcherR = EXTRA_TAGS_DROP.matcher(rightPosTag);
        if( matcherR.find() ) {
          rightPosTag = matcherR.replaceAll("");
        }

        matcherR = EXTRA_TAGS.matcher(rightPosTag);
        if( matcherR.find() ) {
          rightPosTag = matcherR.replaceAll("");
        }
        
        if (stripPerfImperf(leftPosTag).equals(stripPerfImperf(rightPosTag)) 
            && (IPOSTag.startsWith(leftPosTag, IPOSTag.numr, IPOSTag.adv, IPOSTag.adj, IPOSTag.verb)
            || (IPOSTag.startsWith(leftPosTag, IPOSTag.intj, IPOSTag.onomat) 
                && leftAnalyzedToken.getLemma().equalsIgnoreCase(rightAnalyzedToken.getLemma())) ) ) {
          String newPosTag = leftPosTag + extraNvTag + leftPosTagExtra;

          if( (leftPosTag.contains("adjp") && ! rightPosTag.contains("adjp"))
              || (! leftPosTag.contains("adjp") && rightPosTag.contains("adjp")) ) {
            newPosTag = newPosTag.replaceFirst(":&adjp:(actv|pasv):(im)?perf", "");
          }
          
          String newLemma = leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma();
          newAnalyzedTokens.add(new AnalyzedToken(word, newPosTag, newLemma));
        }
        // noun-noun
        else if ( leftPosTag.startsWith(IPOSTag.noun.getText()) && rightPosTag.startsWith(IPOSTag.noun.getText()) ) {

        	// discard ????????????-???????????? as noun:anim
        	if( leftAnalyzedToken.getToken().equalsIgnoreCase(rightAnalyzedToken.getToken())
        			&& leftPosTag.contains(TAG_ANIM) && rightPosTag.contains(TAG_ANIM) )
        		continue;
        	
          String agreedPosTag = getAgreedPosTag(leftPosTag, rightPosTag, leftNv, word);

          if( agreedPosTag == null 
              && rightPosTag.startsWith("noun:inanim:m:v_naz")
              && isMinMax(rightAnalyzedToken.getToken()) ) {
            agreedPosTag = leftPosTag;
          }

          if( agreedPosTag == null && ! isSameAnimStatus(leftPosTag, rightPosTag) ) {

            agreedPosTag = tryAnimInanim(leftPosTag, rightPosTag, leftAnalyzedToken.getLemma(), rightAnalyzedToken.getLemma(), leftNv, rightNv, word);
            
            if( agreedPosTag == null ) {
              animInanimNotTagged = leftPosTag.contains(":anim") ? "anim-inanim" : "inanim-anim";
            }
            else {
              newAnalyzedTokensAnimInanim.add(new AnalyzedToken(word, agreedPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
              continue;
            }
          }
          
          if( agreedPosTag != null ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
          }
        }
        // numr-numr: ????????-??????
        else if ( leftPosTag.startsWith(IPOSTag.numr.getText()) && rightPosTag.startsWith(IPOSTag.numr.getText()) ) {
            String agreedPosTag = getNumAgreedPosTag(leftPosTag, rightPosTag, leftNv);
            if( agreedPosTag != null ) {
              newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
            }
        }
        // noun-numr match
        else if ( IPOSTag.startsWith(leftPosTag, IPOSTag.noun) && IPOSTag.startsWith(rightPosTag, IPOSTag.numr) ) {
          // gender tags match
          String leftGenderConj = PosTagHelper.getGenderConj(leftPosTag);
          if( leftGenderConj != null && leftGenderConj.equals(PosTagHelper.getGenderConj(rightPosTag)) ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
            // ????????????-?????????????? ???????? ???????? ???? ?????????????? ?????? ?? ????????????????: ???????????? ????????????-??????????????, ???????????? ????????????-??????????????
            if( ! leftPosTag.contains(":p:") ) {
              newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag.replaceAll(":[mfn]:", ":p:") + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
            }
          }
          else {
            // (with different gender tags): ?????????? (:p:) - ?????? (:f:)
            String agreedPosTag = getNumAgreedPosTag(leftPosTag, rightPosTag, leftNv);
            if( agreedPosTag != null ) {
              newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
              // ??????-?????? ???????? ???????? ???? ?????????????? ?????? ?? ????????????????: ?????????????? ??????-??????, ???????????? ??????-??????
              if( ! agreedPosTag.contains(":p:") ) {
                newAnalyzedTokens.add(new AnalyzedToken(word, agreedPosTag.replaceAll(":[mfn]:", ":p:") + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
              }
            }
          }
        }
        // noun-adj match: ??????-????????????????, ??????-??????
        // ???? ???????????? ????????????-???????????????????????? ??? ?????????????????? noun-adj ?????? ???????????????? ?????????????????? ??????????????????????????
        else if( leftPosTag.startsWith(IPOSTag.noun.getText()) 
            && IPOSTag.startsWith(rightPosTag, IPOSTag.numr) 
                || (IPOSTag.startsWith(rightPosTag, IPOSTag.adj) && isJuniorSenior(leftAnalyzedToken, rightAnalyzedToken)) ) {
          
//          if( ! leftPosTag.contains(":prop")
//              || isJuniorSenior(leftAnalyzedToken, rightAnalyzedToken) ) { 
          	
          	// discard ????????????-???????????? as noun:anim
//          	if( leftAnalyzedToken.getToken().equalsIgnoreCase(rightAnalyzedToken.getToken()) )
//          		continue;

          String leftGenderConj = PosTagHelper.getGenderConj(leftPosTag);
          if( leftGenderConj != null && leftGenderConj.equals(PosTagHelper.getGenderConj(rightPosTag)) ) {
            newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightAnalyzedToken.getLemma()));
          }
  //        }
        }
        // ??????????-??????????
        else if( leftPosTag.startsWith(IPOSTag.noun.getText()) 
                && rightAnalyzedToken.getLemma().equals("????????????")
                ) {
          String leftGenderConj = PosTagHelper.getGenderConj(leftPosTag);
          if( leftGenderConj != null && leftGenderConj.equals(PosTagHelper.getGenderConj(rightPosTag)) ) {
            String rightLemma = leftGenderConj.startsWith("m") ? "????????????" :
              leftGenderConj.startsWith("f") ? "??????????" : "??????????";
            newAnalyzedTokens.add(new AnalyzedToken(word, leftPosTag + extraNvTag + leftPosTagExtra, leftAnalyzedToken.getLemma() + "-" + rightLemma));
          }
        }
      }
    }

    if( ! newAnalyzedTokens.isEmpty() 
        && ! PosTagHelper.hasPosTagPart(newAnalyzedTokens, ":p:") ) {
      if( (LemmaHelper.hasLemma(leftAnalyzedTokens, LemmaHelper.DAYS_OF_WEEK) && LemmaHelper.hasLemma(rightAnalyzedTokens, LemmaHelper.DAYS_OF_WEEK))
          || (LemmaHelper.hasLemma(leftAnalyzedTokens, LemmaHelper.MONTH_LEMMAS) && LemmaHelper.hasLemma(rightAnalyzedTokens, LemmaHelper.MONTH_LEMMAS)) ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, newAnalyzedTokens.get(0).getPOSTag().replaceAll(":[mfn]:", ":p:"), newAnalyzedTokens.get(0).getLemma()));
      }
    }
    
    // remove duplicates
    newAnalyzedTokens = new ArrayList<>(new LinkedHashSet<>(newAnalyzedTokens));
    
    if( newAnalyzedTokens.isEmpty() ) {
      newAnalyzedTokens = newAnalyzedTokensAnimInanim;
    }

    if( animInanimNotTagged != null && newAnalyzedTokens.isEmpty() ) {
      compoundDebugLogger.logUnknownCompound(word + " " + animInanimNotTagged);
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }


  private static String stripPerfImperf(String leftPosTag) {
    return leftPosTag.replaceAll(":(im)?perf|:&adjp:(actv|pasv)", "");
  }


  private boolean isJuniorSenior(AnalyzedToken leftAnalyzedToken, AnalyzedToken rightAnalyzedToken) {
    return leftAnalyzedToken.getPOSTag().matches(".*?:[flp]name.*") && rightAnalyzedToken.getLemma().matches(".*(????????????????|??????????????)");
  }

  // right part is numr
  @Nullable
  private String getNumAgreedPosTag(String leftPosTag, String rightPosTag, boolean leftNv) {
    String agreedPosTag = null;
    
    if( leftPosTag.contains(":p:") && SING_REGEX_F.matcher(rightPosTag).find()
        || SING_REGEX_F.matcher(leftPosTag).find() && rightPosTag.contains(":p:")) {
      String leftConj = PosTagHelper.getConj(leftPosTag);
      if( leftConj != null && leftConj.equals(PosTagHelper.getConj(rightPosTag)) ) {
        agreedPosTag = leftPosTag;
      }
    }
    return agreedPosTag;
  }

  @Nullable
  private String getAgreedPosTag(String leftPosTag, String rightPosTag, boolean leftNv, String word) {
    boolean leftPlural = isPlural(leftPosTag);
    boolean rightPlural = isPlural(rightPosTag);
      if (leftPlural != rightPlural)
        return null;
    
    if( ! isSameAnimStatus(leftPosTag, rightPosTag) )
      return null;
    
    Matcher stdNounMatcherLeft = stdNounTagRegex.matcher(leftPosTag);
    if( stdNounMatcherLeft.matches() ) {
      Matcher stdNounMatcherRight = stdNounTagRegex.matcher(rightPosTag);
      if (stdNounMatcherRight.matches()) {
        String substring1 = stdNounMatcherLeft.group(2); //leftPosTag.substring(stdNounTagLen, stdNounTagLen + 3);
        String substring2 = stdNounMatcherRight.group(2); //rightPosTag.substring(stdNounTagLen, stdNounTagLen + 3);
        if( substring1.equals(substring2) ) {
          if( ! stdNounMatcherLeft.group(1).equals(stdNounMatcherRight.group(1)) ) {
            compoundDebugLogger.logGenderMix(word, leftNv, leftPosTag, rightPosTag);
            // yes for ??????????????????-??????????????
            // no for ??????-??????????
            if( word.length() < 10 )
              return null;
          }
          
          if( leftNv )
            return rightPosTag;

          return leftPosTag;
        }
      }
    }

    return null;
  }

  private static boolean isMinMax(String rightToken) {
    return rightToken.equals("????????????????")
        || rightToken.equals("??????????????");
  }

  @Nullable
  private String tryAnimInanim(String leftPosTag, String rightPosTag, String leftLemma, String rightLemma, boolean leftNv, boolean rightNv, String word) {
    String agreedPosTag = null;
    
    // ????????????????????????-??????????????
    if( leftMasterSet.contains(leftLemma) ) {
      if( leftPosTag.contains(TAG_ANIM) ) {
        rightPosTag = rightPosTag.replace(TAG_INANIM, TAG_ANIM);
      }
      else {
        rightPosTag = rightPosTag.replace(TAG_ANIM, TAG_INANIM);
      }
      
      agreedPosTag = getAgreedPosTag(leftPosTag, rightPosTag, leftNv, word);
      
      if( agreedPosTag == null ) {
        if (! leftPosTag.contains(TAG_ANIM)) {
          if (MNP_ZNA_REGEX.matcher(leftPosTag).matches() && MNP_NAZ_REGEX.matcher(rightPosTag).matches()
              && ! leftNv && ! rightNv ) {
            agreedPosTag = leftPosTag;
          }
        }
        else {
          if (MNP_ZNA_REGEX.matcher(leftPosTag).matches() && MNP_ROD_REGEX.matcher(rightPosTag).matches()
              && ! leftNv && ! rightNv ) {
            agreedPosTag = leftPosTag;
          }
        }
      }
      
    }
    // ??????????-??????????????
    else if ( slaveSet.contains(rightLemma) ) {
      rightPosTag = rightPosTag.replace(":anim", ":inanim");
      agreedPosTag = getAgreedPosTag(leftPosTag, rightPosTag, false, word);
      if( agreedPosTag == null ) {
        if (leftPosTag.contains(TAG_INANIM)) {
          if (MNP_ZNA_REGEX.matcher(leftPosTag).matches() && MNP_NAZ_REGEX.matcher(rightPosTag).matches()
              && PosTagHelper.getNum(leftPosTag).equals(PosTagHelper.getNum(rightPosTag))
              && ! leftNv && ! rightNv ) {
            agreedPosTag = leftPosTag;
          }
        }
      }
    }
    // ??????????????-??????????
    else if ( slaveSet.contains(leftLemma) ) {
      leftPosTag = leftPosTag.replace(":anim", ":inanim");
      agreedPosTag = getAgreedPosTag(rightPosTag, leftPosTag, false, word);
      if( agreedPosTag == null ) {
        if ( rightPosTag.contains(TAG_INANIM) ) {
          if (MNP_ZNA_REGEX.matcher(rightPosTag).matches() && MNP_NAZ_REGEX.matcher(leftPosTag).matches()
              && PosTagHelper.getNum(leftPosTag).equals(PosTagHelper.getNum(rightPosTag))
              && ! leftNv && ! rightNv ) {
            agreedPosTag = rightPosTag;
          }
        }
      }
    }
    // else
    // ????????????-??????????????????, ??????????????-??????????????, ????????????-??????????, ??????????????????-????????????????
    
    return agreedPosTag;
  }

  private static boolean isSameAnimStatus(String leftPosTag, String rightPosTag) {
    boolean leftAnim = leftPosTag.contains(TAG_ANIM);
    boolean rightAnim = rightPosTag.contains(TAG_ANIM);
    return leftAnim == rightAnim;
  }

  private static boolean isPlural(String posTag) {
    return posTag.startsWith("noun:") && posTag.contains(":p:");
  }

  @Nullable
  private List<AnalyzedToken> oAdjMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(analyzedTokens.size());

    String leftBase = leftWord.substring(0, leftWord.length()-1);
    
    String extraTag = "";
    if( ! LEFT_O_ADJ.contains(leftWord.toLowerCase(conversionLocale)) ) {

      List<TaggedWord> taggedWords = new ArrayList<>();

      // ?????????????? ?????? ??????????????-??????????????????, ??????-????????????????????????????
      taggedWords = tagBothCases(leftWord, Pattern.compile("^adv.*|.*?numr.*"));
      if( taggedWords.isEmpty() ) {
        taggedWords = tagBothCases(oToYj(leftWord), Pattern.compile("^adj.*"));  // ???????????????? ?????? ??????????????-????????????????
      }
      if( taggedWords.isEmpty() ) {
        taggedWords = tagBothCases(leftBase, Pattern.compile("^noun.*"));         // ?????????????? ?????? ????????????????-??????????????????????
      }
      if( taggedWords.isEmpty() ) {
        // ?????? ?????? ??????-??????????????????????, ??????????-????????????????????????
        taggedWords = tagBothCases(leftBase + "??", Pattern.compile("(noun:inanim:f:v_naz|numr).*"));   
      }
      if( taggedWords.isEmpty() )
        return null;

      // ???????????????????????????????????? - ??????????
      if(! extraTag.equals(":bad") && taggedWords.get(0).getPosTag().startsWith(IPOSTag.adv.getText())
          && PosTagHelper.hasPosTagPart(analyzedTokens, "adjp")) {
        extraTag = ":bad";
      }

      if (! extraTag.equals(":bad") && PosTagHelper.hasPosTagPart2(taggedWords, ":bad")) {
          extraTag = ":bad";
      }
    }

    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( IPOSTag.adj.getText() ) ) {
        if( posTag.contains(":comp") ) {
          posTag = PosTagHelper.ADJ_COMP_REGEX.matcher(posTag).replaceFirst("");
        }
        if( ! posTag.contains(":bad") ) {
          posTag += extraTag;
        }
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord.toLowerCase() + "-" + analyzedToken.getLemma()));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

    @Nullable
    private List<AnalyzedToken> numrAdjMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord) {
      List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(analyzedTokens.size());

      String extraTag = "";

      List<TaggedWord> taggedWords = wordTagger.tag(leftWord);
      if( ! PosTagHelper.hasPosTagStart2(taggedWords, "numr") )
        return null;

      // ????????-?????????????????????????? - bad
      if( leftWord.matches(".*?(????????|??????????|????????????????)") ) {
        //        taggedWords = wordTagger.tag("??????");
        extraTag = ":bad";
      }

      for (AnalyzedToken analyzedToken : analyzedTokens) {
        String posTag = analyzedToken.getPOSTag();
        if( posTag.startsWith( IPOSTag.adj.getText() ) ) {
          if( posTag.contains(":comp") ) {
            posTag = PosTagHelper.ADJ_COMP_REGEX.matcher(posTag).replaceFirst("");
          }
          if( ! posTag.contains(":bad") ) {
            posTag += extraTag;
          }
          newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord.toLowerCase() + "-" + analyzedToken.getLemma()));
        }
      }

      return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
    }

  private static String oToYj(String leftWord) {
    return leftWord.endsWith("????") 
        ? leftWord.substring(0, leftWord.length()-2) + "????" 
        : leftWord.substring(0,  leftWord.length()-1) + "????";
  }

  @Nullable
  private static List<AnalyzedToken> getNvPrefixNounMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord, String extraTag) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(analyzedTokens.size());

    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith(IPOSTag.noun.getText() )
          && ! posTag.contains("v_kly") ) {

//        if( Arrays.asList("??2??", "????", "??????????????????", "??????????").contains(leftWord) ) {
//            posTag = PosTagHelper.addIfNotContains(posTag, ":bad");
//        }

        // ????????-???????? - ok for ua_2019 too
        if( ! extraTag.equals(":ua_1992") || ! Character.isUpperCase(analyzedToken.getLemma().charAt(0)) ) {
          if( StringUtils.isNotEmpty(extraTag) ) {
            posTag = PosTagHelper.addIfNotContains(posTag, extraTag);
          }
        }

        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord + "-" + analyzedToken.getLemma()));
      }
    }

    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  @Nullable
  private static List<AnalyzedToken> getNvPrefixLatWithAdjMatch(String word, List<AnalyzedToken> analyzedTokens, String leftWord) {
    List<AnalyzedToken> newAnalyzedTokens = new ArrayList<>(analyzedTokens.size());
    
    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
         if( posTag.startsWith(IPOSTag.adj.getText())    // n-?????????????????? 
             && ! posTag.contains("v_kly") ) {
        newAnalyzedTokens.add(new AnalyzedToken(word, posTag, leftWord + "-" + analyzedToken.getLemma()));
      }
    }
    
    return newAnalyzedTokens.isEmpty() ? null : newAnalyzedTokens;
  }

  @Nullable
  private List<AnalyzedToken> poAdvMatch(String word, List<AnalyzedToken> analyzedTokens, String adjTag) {
    
    for (AnalyzedToken analyzedToken : analyzedTokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag.startsWith( adjTag ) ) {
        return Arrays.asList(new AnalyzedToken(word, IPOSTag.adv.getText(), word));
      }
    }
    
    return null;
  }


  private String capitalize(String word) {
    return word.substring(0, 1).toUpperCase(conversionLocale) + word.substring(1);
  }

  private List<TaggedWord> tagBothCases(String leftWord, Pattern posTagMatcher) {
    List<TaggedWord> leftWdList = wordTagger.tag(leftWord);
    
    String leftLowerCase = leftWord.toLowerCase(conversionLocale);
    if( ! leftWord.equals(leftLowerCase)) {
      leftWdList.addAll(wordTagger.tag(leftLowerCase));
    }
    else {
      String leftUpperCase = capitalize(leftWord);
      if( ! leftWord.equals(leftUpperCase)) {
        leftWdList.addAll(wordTagger.tag(leftUpperCase));
      }
    }
    
    if( posTagMatcher != null ) {
      leftWdList = leftWdList.stream()
          .filter(word -> posTagMatcher.matcher(word.getPosTag()).matches())
          .collect(Collectors.toList());
    }

    return leftWdList;
  }

  private List<TaggedWord> tagAsIsAndWithLowerCase(String leftWord) {
    List<TaggedWord> leftWdList = wordTagger.tag(leftWord);
    
    String leftLowerCase = leftWord.toLowerCase(conversionLocale);
    if( ! leftWord.equals(leftLowerCase)) {
      leftWdList.addAll(wordTagger.tag(leftLowerCase));
    }

    return leftWdList;
  }

  
  @Nullable
  List<AnalyzedToken> guessOtherTags(String word) {
    List<AnalyzedToken> guessedTokens = guessOtherTagsInternal(word);
    compoundDebugLogger.logTaggedCompound(guessedTokens);
    return guessedTokens;
  }
  
  @Nullable
  private List<AnalyzedToken> guessOtherTagsInternal(String word) {
    if( word.length() <= 7 
        || ! UKR_LETTERS_PATTERN.matcher(word).matches() )
      return null;

    if( StringTools.isCapitalizedWord(word) ) {

      if (word.endsWith("??????????????")
          || word.endsWith("????????????")) {
        String addPos = word.endsWith("??????????????") ? ":alt" : "";
        return PosTagHelper.generateTokensForNv(word, "f", ":prop" + addPos);
      }

      if (word.endsWith("??????")
          || word.endsWith("??????????")) {
        return PosTagHelper.generateTokensForNv(word, "mf", ":prop:lname");
      }
      
    }
    
    String lowerCase = word.toLowerCase();
    for(String prefix: dashPrefixesInvalid) {
      // mostly false compounds
      if( prefix.equals("????????") )
        continue;

      if( lowerCase.startsWith(prefix) ) {
        String right = word.substring(prefix.length(), word.length());

        String apo = "";
        String addTag = null;

        if( right.startsWith("'") ) { 
          right = right.substring(1);
          apo = "'";
        }
        
        if( right.length() < 2 )
          continue;
          

        boolean apoNeeded = false;
        if( "????????".indexOf(right.charAt(0)) != -1
            && "????????????????????".indexOf(prefix.charAt(prefix.length()-1)) == -1) {
          apoNeeded = true;
        }
        if( apoNeeded == apo.isEmpty() ){
          addTag = ":bad";
        }

        if( right.length() >= 4 && ! StringTools.isCapitalizedWord(right) ) {
          List<TaggedWord> rightWdList = wordTagger.tag(right);
          rightWdList = PosTagHelper.filter2(rightWdList, PREFIX_NO_DASH_POSTAG_PATTERN);
          rightWdList.removeIf(w -> w.getPosTag().startsWith("noun:inanim") && w.getPosTag().contains("v_kly"));

          if( rightWdList.size() > 0 ) {
            rightWdList = PosTagHelper.adjust(rightWdList, addTag, prefix+apo);

            List<AnalyzedToken> compoundTokens = ukrainianTagger.asAnalyzedTokenListForTaggedWordsInternal(word, rightWdList);
            return compoundTokens;
          }
        }
      }
    }
  
    return null;
  }

}