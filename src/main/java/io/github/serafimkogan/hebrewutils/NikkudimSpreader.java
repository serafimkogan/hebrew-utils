package io.github.serafimkogan.hebrewutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class NikkudimSpreader {
	private static final String ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE = "Unable to spread diacritics from given source, most likely completely different words have been provided";
	private static final String NULL_POINTER_EXCEPTION_MESSAGE = "Given arguments shouldn't be null";
	
	private String reference;
    private String billet;
	private ArrayList<Change> changes = new ArrayList<>();
	private String result;
     
    public NikkudimSpreader(String reference, String billet) {
    	if (reference == null || billet == null)
    		throw new NullPointerException(NULL_POINTER_EXCEPTION_MESSAGE);  	
    	
    	this.reference = reference;
    	this.billet = billet;

		TokenList referenceTokens = new TokenList(this.reference, false);
		TokenList billetTokens = new TokenList(this.billet, false);
		
		if (billetTokens.size() != referenceTokens.size()) 
			throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE);
			
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < billetTokens.size(); i++) {
			Change change = new Change(referenceTokens.get(i), billetTokens.get(i));
    		changes.add(change);
    		builder.append(change.after);
		}
		
		result = builder.toString();
    }
    
    public String getResult() {
        return result;
    }
    
    public String getBillet() {
        return billet;
    }
    
    public String getReference() {
        return reference;
    }
    
	public String getMetaData() {
		StringBuilder builder = new StringBuilder();
		
		for (Change change : changes) {
			builder.append(change.getMetaData());
			builder.append(Symbols.next_line);
		}
			
		builder.append("before: ");
		builder.append(billet);
		builder.append(Symbols.next_line);

		builder.append("after: ");
		builder.append(result);
		builder.append(" | cyrillization: ");
		builder.append(new Cyrillizer(result).getResult());
		
		builder.append(Symbols.next_line);
		
		builder.append("reference: ");
		builder.append(reference);
		builder.append(" | cyrillization: ");
		builder.append(new Cyrillizer(reference).getResult());

		return builder.toString();
	}

	private enum Sequence {
		VAV("ו"),
		YUD("י"),
		NULL(""),
		YUDYUD("יי"),
		VAVYUD("וי"),
		YUDVAV("יו"),
		VAVVAV("וו"),
		YUDVAVVAV("יוו"),
		VAVYUDVAV("ויו"),
		VAVYUDYUD("ויי"),
		VAVVAVYUD("ווי"),
		YUDVAVYUD("יוי"),
		YUDVAVVAVYUD("יווי"),
		YUDVAVYUDVAV("יויו"),
		VAVVAVYUDVAV("וויו"),
		VAVVAVYUDYUD("וויי"),
		OTHER("");
		
		final String sequence;
		Sequence(String sequence) {
			this.sequence = sequence;
		}
		
		static Sequence getSequence(String string) {
			for (Sequence sequence : values()) {
				if (sequence.sequence.equals(string)) {
					return sequence;
				}
			}
			
			return OTHER;
		}
	}
	
	private enum ChangeReason {
    	REGULAR(Sequence.NULL, Sequence.NULL),
    	VAV_TO_NULL(Sequence.VAV, Sequence.NULL),
    	YUD_TO_NULL(Sequence.YUD, Sequence.NULL),
    	NULL_TO_YUD(Sequence.NULL, Sequence.YUD),
    	YUDYUD_TO_YUD(Sequence.YUDYUD, Sequence.YUD),
    	NULL_TO_YUDYUD(Sequence.NULL, Sequence.YUDYUD),
    	YUD_TO_YUDYUD(Sequence.YUD, Sequence.YUDYUD),
    	VAV_TO_YUDVAV(Sequence.VAV, Sequence.YUDVAV),
    	YUD_TO_YUDVAV(Sequence.YUD, Sequence.YUDVAV),
    	VAVYUD_TO_YUDVAVYUD(Sequence.VAVYUD, Sequence.YUDVAVYUD),
    	YUDVAV_TO_YUDVAVYUD(Sequence.YUDVAV, Sequence.YUDVAVYUD),
    	VAVVAV_TO_YUDVAVVAV(Sequence.VAVVAV, Sequence.YUDVAVVAV),
    	VAVYUD_TO_VAVVAVYUD(Sequence.VAVYUD, Sequence.VAVVAVYUD),
    	VAV_TO_YUDVAVVAV(Sequence.VAV, Sequence.YUDVAVVAV),
    	YUDVAVVAV_TO_YUDVAVYUDVAV(Sequence.YUDVAVVAV, Sequence.YUDVAVYUDVAV),
    	YUDVAVVAVYUD_TO_YUDVAVYUD(Sequence.YUDVAVVAVYUD, Sequence.YUDVAVYUD),
    	NULL_TO_VAV(Sequence.NULL, Sequence.VAV),
    	VAVVAV_TO_VAV(Sequence.VAVVAV, Sequence.VAV),
    	YUDVAV_TO_VAV(Sequence.YUDVAV, Sequence.VAV),
    	NULL_TO_VAVVAV(Sequence.NULL, Sequence.VAVVAV),
    	VAV_TO_VAVVAV(Sequence.VAV, Sequence.VAVVAV),
    	YUD_TO_VAVYUDYUD(Sequence.YUD, Sequence.VAVYUDYUD),
    	YUD_TO_VAVYUD(Sequence.YUD, Sequence.VAVYUD),
    	YUDVAV_TO_VAVYUDVAV(Sequence.YUDVAV, Sequence.VAVYUDVAV),
    	YUDVAV_TO_VAVVAVYUD(Sequence.YUDVAV, Sequence.VAVVAVYUD),
    	VAV_TO_VAVVAVYUD(Sequence.VAV, Sequence.VAVVAVYUD),
    	VAVYUDVAV_TO_VAVVAVYUDVAV(Sequence.VAVYUDVAV, Sequence.VAVVAVYUDVAV),
		VAVYUDYUD_TO_VAVVAVYUDYUD(Sequence.VAVYUDYUD, Sequence.VAVVAVYUDYUD),
		VAVYUD_TO_VAVVAVYUDYUD(Sequence.VAVYUD, Sequence.VAVVAVYUDYUD),
		VAVVAVYUD_TO_VAVVAVYUDYUD(Sequence.VAVVAVYUD, Sequence.VAVVAVYUDYUD),
    	OTHER(Sequence.NULL, Sequence.NULL);

		final Sequence from; 
		final Sequence to; 
		ChangeReason(Sequence from, Sequence to) {
			this.from = from;
			this.to = to;
		}

		static ChangeReason getChangeReason(Sequence from, Sequence to) {
			for (ChangeReason reason : values()) {
				if (from == reason.from && to == reason.to) {
					return reason;
				}
			}
			
			return OTHER;
		}
	}
	
	@SuppressWarnings("serial")
	private static class TokenList extends ArrayList<Token> {
		private String source;
	    private String stringToTokenize;
	    private HashMap<Integer, String> tokens = new HashMap<>();
	    
	    private TokenList(String stringToTokenize, boolean separateYudsAndVavs) {
	    	this.source = stringToTokenize;
	        this.stringToTokenize = stringToTokenize
	                .replace(Symbols.Hebrew.abbreviation_two_gereshs, String.valueOf(Symbols.dot))
	                .replace(Symbols.Hebrew.abbreviation_two_apostrophes, String.valueOf(Symbols.dot))
	                .replace(Symbols.Hebrew.abbreviation_quotation_mark, Symbols.dot)
	                .replace(Symbols.Hebrew.abbreviation_gershayim, Symbols.dot)
	                .replace("-", "־");

	        tokenizePredefinedCombination(" ");
	        tokenizePredefinedCombination(".");
	        tokenizePredefinedCombination("־");
	        tokenizeLetters(separateYudsAndVavs);
	        tokenizeDigits();

	        generateTokensSet();
		}
		
		public String toString() {
			StringBuilder builder = new StringBuilder();
			for (Token token : this) 
				builder.append(token.toString());
			
			return builder.toString();
		}
		
		private String toStringAsTokenList() {
			StringBuilder builder = new StringBuilder();
			for (Token token : this) 
				builder.append(token.toStringAsToken());
			
			return builder.toString();
		}
		
		private void generateTokensSet() {
	        for (int i = 0; i < stringToTokenize.length(); i++) 
	            if (tokens.get(i) != null) 
	            	this.add(new Token(tokens.get(i)));
	    }
		
	    private void tokenizePredefinedCombination(String combination) {
	        if (!stringToTokenize.contains(combination))
	            return;
	    	
	        for (int i = 0; i < stringToTokenize.length() - combination.length() + 1; i++) {
	            if (stringToTokenize.substring(i, i + combination.length()).equals(combination)) {
	                tokens.put(i, combination);
	            }
	        }

	        StringBuilder spaceBuilder = new StringBuilder();
	        for (int i = 0; i < combination.length(); i++)
	            spaceBuilder.append(" ");
	        stringToTokenize = stringToTokenize.replace(combination, spaceBuilder);
	    }
	    
	    private void tokenizeLetters(boolean separateYudsAndVavs) {
	        for (int i = 0; i < stringToTokenize.length(); i++) {
	            if (Symbols.Hebrew.isHebrewLetter(stringToTokenize.charAt(i))) {

	                StringBuilder builder = new StringBuilder();
	                builder.append(stringToTokenize.charAt(i));

	                if (i == stringToTokenize.length() - 1) {
	                    tokens.put(i, builder.toString());
	                } else {
	                    for (int j = i + 1; j <= stringToTokenize.length(); j++) {
	                        if (j != stringToTokenize.length()
	                                && (Symbols.Hebrew.isHebrewDiacritics(stringToTokenize.charAt(j))
	                                || stringToTokenize.charAt(j) == Symbols.Hebrew.geresh
	                                || stringToTokenize.charAt(j) == Symbols.apostrophe
	                                || !separateYudsAndVavs && stringToTokenize.charAt(j) == 'ו'
	                                || !separateYudsAndVavs && stringToTokenize.charAt(j) == 'י')) {
	                            builder.append(stringToTokenize.charAt(j));
	                        } else {
	                            tokens.put(i, builder.toString());
	                            i = j - 1;
	                            break;
	                        }
	                    }
	                }
	            }
	        }
	    }

	    private void tokenizeDigits() {
	        for (int i = 0; i < stringToTokenize.length(); i++) {
	            if (Character.isDigit(stringToTokenize.charAt(i))) {

	                StringBuilder builder = new StringBuilder();
	                builder.append(stringToTokenize.charAt(i));

	                if (i == stringToTokenize.length() - 1) {
	                    tokens.put(i, builder.toString());
	                } else {
	                    for (int j = i + 1; j <= stringToTokenize.length(); j++) {
	                        if (j == stringToTokenize.length() || !Character.isDigit(stringToTokenize.charAt(j))) {
	                            tokens.put(i, builder.toString());
	                            i = j - 1;
	                            break;
	                        } else {
	                            builder.append(stringToTokenize.charAt(j));
	                        }
	                    }
	                }
	            }
	        }
	    }

	}
	
	private static class Token {
        private String root;
        private String fullToken;
        private ArrayList<Character> nikkudim = new ArrayList<>();
        private boolean isHebrewToken = false;
        
        private Token(String string) {
        	nikkudim.clear();
        	
            fullToken = string;

            StringBuilder rootBuilder = new StringBuilder();
            char firstChar = string.charAt(0);
            if (Symbols.Hebrew.isHebrewLetter(firstChar)) {
                isHebrewToken = true;
                
                for (char ch : string.toCharArray()) {
                    if (Symbols.Hebrew.isHebrewLetter(ch)) {
                        rootBuilder.append(ch);
                    } else {
                        nikkudim.add(ch);
                    }
                }
                root = rootBuilder.toString();
            } else {
                root = fullToken;
            }
            
            Collections.sort(nikkudim);
        }
        
        public String toString() {
        	StringBuilder builder = new StringBuilder();
        	
        	builder.append(this.root);
			for (char nikkud : this.nikkudim)
				builder.append(nikkud);
			
			return builder.toString();
        }
        
        private String toStringAsToken() {
        	StringBuilder builder = new StringBuilder();
        	
        	builder.append("[");
			builder.append(this.toString());
			builder.append("]");
			
			return builder.toString();
        }
    }
	
	private static class Change {
		private ChangeReason reason = ChangeReason.OTHER;
		private String before;
		private String beforeAsTokenList;
		private String after;
		private String afterAsTokenList;
		private String reference;
		private String referenceAsTokenList;
		
		private Change(Token referenceToken, Token billetToken) {
			TokenList billetTokenList = new TokenList(billetToken.fullToken, true);
			TokenList referenceTokenList = new TokenList(referenceToken.fullToken, true);
		    
			if (billetTokenList.get(0).isHebrewToken && !billetTokenList.get(0).root.equals(referenceTokenList.get(0).root)) {
				throw new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE
						+ ", token \"" + billetTokenList.get(0).root + "\""
						+ " doesn't match with token \"" + referenceTokenList.get(0).root + "\"");
			}
			
			this.before = billetTokenList.toString();
			this.beforeAsTokenList = billetTokenList.toStringAsTokenList();
			this.reference = referenceTokenList.toString();
			this.referenceAsTokenList = referenceTokenList.toStringAsTokenList();
			
			String referenceWithoutDiacritics = Symbols.Hebrew.removeDiacritics(referenceTokenList.source);
			Sequence root = Sequence.getSequence(referenceWithoutDiacritics.substring(1).replace(String.valueOf(Symbols.apostrophe), ""));
			Sequence key = Sequence.getSequence(billetTokenList.source.substring(1).replace(String.valueOf(Symbols.apostrophe), ""));
			
			
			reason = ChangeReason.getChangeReason(root, key);
			
			if (referenceWithoutDiacritics.equals(billetTokenList.source)) {
				reason = ChangeReason.REGULAR;
				after = referenceToken.fullToken;
				afterAsTokenList = referenceToken.toStringAsToken();
			} else {
				switch (reason) {
					case REGULAR: {
						return;
					}
					case VAV_TO_NULL: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						break;
					}
					case YUD_TO_NULL: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						if (!billetTokenList.get(0).nikkudim.contains(Symbols.Hebrew.hiriq))
							billetTokenList.get(0).nikkudim.add(Symbols.Hebrew.hiriq);
						break;
					}
					case NULL_TO_YUD: {
						break;
					}
					case YUDYUD_TO_YUD: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						break;
					}
					case NULL_TO_YUDYUD: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						break;
					}
					case YUD_TO_YUDYUD: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						break;
					}
					case VAV_TO_YUDVAV: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);	
						billetTokenList.get(2).nikkudim.removeIf(ch -> (ch == Symbols.Hebrew.hiriq));
						break;
					}
					case YUD_TO_YUDVAV: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						billetTokenList.get(2).nikkudim.removeIf(ch -> (ch == Symbols.Hebrew.dagesh));
						break;
					}
					case VAVYUD_TO_YUDVAVYUD: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						billetTokenList.get(3).nikkudim.addAll(referenceTokenList.get(2).nikkudim);
						break;
					}
					case YUDVAV_TO_YUDVAVYUD: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(1).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(2).nikkudim);
						break;
					}
					case VAVVAV_TO_YUDVAVVAV: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(3).nikkudim.addAll(referenceTokenList.get(2).nikkudim);
						break;
					}
					case VAV_TO_YUDVAVVAV: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(3).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						break;
					}
					case YUDVAVVAV_TO_YUDVAVYUDVAV: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						billetTokenList.get(4).nikkudim.addAll(referenceTokenList.get(3).nikkudim);
						break;
					}
					case YUDVAVVAVYUD_TO_YUDVAVYUD: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						break;
					}
					case NULL_TO_VAV: {
						if (Symbols.Hebrew.nikkudimContainsNonStressedO(referenceTokenList.get(0).nikkudim)) {
							billetTokenList.get(1).nikkudim.add(Symbols.Hebrew.holam);
							if (referenceTokenList.get(0).nikkudim.contains(Symbols.Hebrew.dagesh))
								billetTokenList.get(0).nikkudim.add(Symbols.Hebrew.dagesh);
			            } else if (referenceTokenList.get(0).nikkudim.contains(Symbols.Hebrew.dagesh)) {
							billetTokenList.get(1).nikkudim.add(Symbols.Hebrew.dagesh);
			            }
						break;
					}
					case VAVVAV_TO_VAV: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(1).nikkudim.addAll(referenceTokenList.get(2).nikkudim);
						break;
					}
					case YUDVAV_TO_VAV: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(0).nikkudim.add(Symbols.Hebrew.hiriq);
						billetTokenList.get(1).nikkudim.addAll(referenceTokenList.get(2).nikkudim);
						break;
					}
					case NULL_TO_VAVVAV: {
						if (Symbols.Hebrew.nikkudimContainsNonStressedO(referenceTokenList.get(0).nikkudim)) {
							billetTokenList.get(1).nikkudim.add(Symbols.Hebrew.holam);
							billetTokenList.get(2).nikkudim.add(Symbols.Hebrew.holam);
							if (referenceTokenList.get(0).nikkudim.contains(Symbols.Hebrew.dagesh))
								billetTokenList.get(0).nikkudim.add(Symbols.Hebrew.dagesh);
			            } else if (referenceTokenList.get(0).nikkudim.contains(Symbols.Hebrew.dagesh)) {
							billetTokenList.get(1).nikkudim.add(Symbols.Hebrew.dagesh);
							billetTokenList.get(2).nikkudim.add(Symbols.Hebrew.dagesh);
			            }
						break;
					}
					case VAV_TO_VAVVAV: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						break;
					}
					case YUD_TO_VAVYUDYUD: {
						if (Symbols.Hebrew.nikkudimContainsNonStressedO(referenceTokenList.get(0).nikkudim)) {
							billetTokenList.get(1).nikkudim.add(Symbols.Hebrew.holam);
							if (referenceTokenList.get(0).nikkudim.contains(Symbols.Hebrew.dagesh))
								billetTokenList.get(0).nikkudim.add(Symbols.Hebrew.dagesh);
			            } else if (referenceTokenList.get(0).nikkudim.contains(Symbols.Hebrew.dagesh)) {
							billetTokenList.get(1).nikkudim.add(Symbols.Hebrew.dagesh);
			            }
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						break;
					}
					case YUD_TO_VAVYUD: {
						if (Symbols.Hebrew.nikkudimContainsNonStressedO(referenceTokenList.get(0).nikkudim)) {
							billetTokenList.get(1).nikkudim.add(Symbols.Hebrew.holam);
							if (referenceTokenList.get(0).nikkudim.contains(Symbols.Hebrew.dagesh))
								billetTokenList.get(0).nikkudim.add(Symbols.Hebrew.dagesh);
			            } else if (referenceTokenList.get(0).nikkudim.contains(Symbols.Hebrew.dagesh)) {
							billetTokenList.get(1).nikkudim.add(Symbols.Hebrew.dagesh);
			            }
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						break;
					}
					case YUDVAV_TO_VAVYUDVAV: {
						if (Symbols.Hebrew.nikkudimContainsNonStressedO(referenceTokenList.get(0).nikkudim)) {
							billetTokenList.get(1).nikkudim.add(Symbols.Hebrew.holam);
							if (referenceTokenList.get(0).nikkudim.contains(Symbols.Hebrew.dagesh))
								billetTokenList.get(0).nikkudim.add(Symbols.Hebrew.dagesh);
			            } else if (referenceTokenList.get(0).nikkudim.contains(Symbols.Hebrew.dagesh)) {
							billetTokenList.get(1).nikkudim.add(Symbols.Hebrew.dagesh);
			            }
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						billetTokenList.get(3).nikkudim.addAll(referenceTokenList.get(2).nikkudim);
						break;
					}
					case YUDVAV_TO_VAVVAVYUD: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						billetTokenList.get(3).nikkudim.addAll(referenceTokenList.get(2).nikkudim);
						break;
					}
					case VAV_TO_VAVVAVYUD: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						break;
					}
					case VAVYUDVAV_TO_VAVVAVYUDVAV: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						billetTokenList.get(3).nikkudim.addAll(referenceTokenList.get(2).nikkudim);
						billetTokenList.get(4).nikkudim.addAll(referenceTokenList.get(3).nikkudim);
						break;
					}
					case VAVYUDYUD_TO_VAVVAVYUDYUD: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						billetTokenList.get(3).nikkudim.addAll(referenceTokenList.get(2).nikkudim);
						billetTokenList.get(4).nikkudim.addAll(referenceTokenList.get(3).nikkudim);
						break;
					}
					case VAVYUD_TO_VAVVAVYUDYUD: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						billetTokenList.get(4).nikkudim.addAll(referenceTokenList.get(2).nikkudim);
						break;
					}
					case VAVVAVYUD_TO_VAVVAVYUDYUD: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(1).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(2).nikkudim);
						billetTokenList.get(3).nikkudim.addAll(referenceTokenList.get(3).nikkudim);
						break;
					}
					case VAVYUD_TO_VAVVAVYUD: {
						billetTokenList.get(0).nikkudim.addAll(referenceTokenList.get(0).nikkudim);
						billetTokenList.get(2).nikkudim.addAll(referenceTokenList.get(1).nikkudim);
						billetTokenList.get(3).nikkudim.addAll(referenceTokenList.get(2).nikkudim);
						break;
					}
					default: {
						
					}
				}
				
				after = billetTokenList.toString();
				afterAsTokenList = billetTokenList.toStringAsTokenList();
			}
		}
    	
		private String getMetaData() {
    		StringBuilder builder = new StringBuilder();
    		builder.append(reason.name());
    		
    		builder.append(" | before: \"");
    		builder.append(before);
    		builder.append("\" (");
    		builder.append(beforeAsTokenList);
    		builder.append(")");
    		
    		builder.append(" | after: \"");
    		builder.append(after);
    		builder.append("\" (");
    		builder.append(afterAsTokenList);
    		builder.append(")");
    		
    		builder.append(" | reference: \"");
    		builder.append(reference);
    		builder.append("\" (");
    		builder.append(referenceAsTokenList);
    		builder.append(")");
    		
    		return builder.toString();
    	}
	}
}
