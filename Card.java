public class Card implements Comparable{
    private int rank;
    private char suit;

    public Card(){
	suit = ' ';
	rank = 0;
    }
    public Card(char theSuit, int theRank){
	this.suit = theSuit;
	this.rank = theRank;
    }
    public int getRank(){
	
	return rank;
    }
    public char getSuit(){
	return suit;
    }
    public String toString(){
	if(rank == 11){
	    return suit + ""+'J';
	}
	else if(rank == 12){
	    return suit+ ""+'Q';
	}
	else if(rank== 13){
	    return suit+""+'K';
	}
	else if(rank == 14){
	    return suit +""+'A';
	}
	else
	    return suit+ ""+ rank;
    }
    @Override
	public int compareTo(Object anotherCard) {
	Card another = (Card)anotherCard;
	return this.rank-another.rank;
    }
}