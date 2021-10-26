package org.minima.system.brains;

import java.util.ArrayList;

import org.minima.database.txpowtree.TxPoWTreeNode;
import org.minima.objects.Coin;

public class TxPoWSearcher {

	public static ArrayList<Coin> getRelevantUnspentCoins(TxPoWTreeNode zStartNode) {
		
		//The list of Coins
		ArrayList<Coin> coinentry = new ArrayList<>();
		
		//Get the tip..
		TxPoWTreeNode tip = zStartNode;
		
		//A list of spent CoinID..
		ArrayList<String> spentcoins = new ArrayList<>();
		
		//Now cycle through and get all your coins..
		while(tip != null) {
			
			//Get the Relevant coins..
			ArrayList<Coin> coins = tip.getRelevantCoins();
			
			//Get the details..
			for(Coin coin : coins) {
				
				//Are we searching for a specific token..
				//..
				
				//Get the CoinID
				String coinid = coin.getCoinID().to0xString();
				
				//is it spent..
				boolean spent = coin.getSpent();
				
				//Add it to our list of spent coins..
				if(spent) {
					spentcoins.add(coinid);
				}else {
					//Check if this has been spent in a previous block..
					if(!spentcoins.contains(coinid)) {
						
						//OK - fresh unspent coin
						coinentry.add(coin);
					}
				}
			}
			
			//And move back up the tree
			tip = tip.getParent();
		}
		
		return coinentry;
	}
		
}