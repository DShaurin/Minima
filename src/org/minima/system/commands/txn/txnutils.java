package org.minima.system.commands.txn;

import java.util.ArrayList;

import org.minima.database.MinimaDB;
import org.minima.database.mmr.MMRProof;
import org.minima.database.txpowtree.TxPoWTreeNode;
import org.minima.database.wallet.KeyRow;
import org.minima.database.wallet.Wallet;
import org.minima.objects.Coin;
import org.minima.objects.CoinProof;
import org.minima.objects.ScriptProof;
import org.minima.objects.Transaction;
import org.minima.objects.Witness;
import org.minima.objects.base.MiniData;
import org.minima.objects.base.MiniNumber;
import org.minima.objects.keys.Signature;
import org.minima.system.params.GlobalParams;
import org.minima.utils.Crypto;

public class txnutils {

	public static void setMMRandScripts(Transaction zTransaction, Witness zWitness) throws Exception {
		//get the tip..
		TxPoWTreeNode tip = MinimaDB.getDB().getTxPoWTree().getTip();
		
		//Get all the input coins..
		ArrayList<Coin> inputs = zTransaction.getAllInputs();
		
		//Min depth of a coin
		MiniNumber minblock = MiniNumber.ZERO;
				
		//Add the inputs..
		for(Coin input : inputs) {
			//How deep
			if(input.getBlockCreated().isMore(minblock)) {
				minblock = input.getBlockCreated();
			}
		}
		
		//Get the block..
		MiniNumber currentblock = tip.getBlockNumber();
		MiniNumber blockdiff 	= currentblock.sub(minblock);
		if(blockdiff.isMore(GlobalParams.MINIMA_MMR_PROOF_HISTORY)) {
			blockdiff = GlobalParams.MINIMA_MMR_PROOF_HISTORY;
		}
		
		//Now get that Block
		TxPoWTreeNode mmrnode = tip.getPastNode(tip.getBlockNumber().sub(blockdiff));
		if(mmrnode == null) {
			//Not enough blocks..
			throw new Exception("Not enough blocks in chain to make valid MMR Proofs..");
		}
		
		//Get the main Wallet
		Wallet walletdb = MinimaDB.getDB().getWallet();
		
		//Add the MMR proofs for the coins..
		for(Coin input : inputs) {
			
			//Get the proof..
			MMRProof proof = mmrnode.getMMR().getProofToPeak(input.getMMREntryNumber());
			
			//Create the CoinProof..
			CoinProof cp = new CoinProof(input, proof);
			
			//Add it to the witness data
			zWitness.addCoinProof(cp);
			
			//Add the script proofs
			String scraddress 	= input.getAddress().to0xString();
			KeyRow keyrow 		= walletdb.getKeysRowFromAddress(scraddress); 
			if(keyrow == null) {
				throw new Exception("SERIOUS ERROR script missing for simple address : "+scraddress);
			}
			
			ScriptProof pscr = new ScriptProof(keyrow.getScript());
			zWitness.addScript(pscr);
		}
	}
	
	
//	public static Witness createWitness(Transaction zTransaction) throws Exception {
//		Witness witness = new Witness();
//
//		//get the tip..
//		TxPoWTreeNode tip = MinimaDB.getDB().getTxPoWTree().getTip();
//		
//		//Get all the input coins..
//		ArrayList<Coin> inputs = zTransaction.getAllInputs();
//		
//		//Min depth of a coin
//		MiniNumber minblock = MiniNumber.ZERO;
//				
//		//Add the inputs..
//		for(Coin input : inputs) {
//			//How deep
//			if(input.getBlockCreated().isMore(minblock)) {
//				minblock = input.getBlockCreated();
//			}
//		}
//		
//		//Get the block..
//		MiniNumber currentblock = tip.getBlockNumber();
//		MiniNumber blockdiff 	= currentblock.sub(minblock);
//		if(blockdiff.isMore(GlobalParams.MINIMA_MMR_PROOF_HISTORY)) {
//			blockdiff = GlobalParams.MINIMA_MMR_PROOF_HISTORY;
//		}
//		
//		//Now get that Block
//		TxPoWTreeNode mmrnode = tip.getPastNode(tip.getBlockNumber().sub(blockdiff));
//		if(mmrnode == null) {
//			//Not enough blocks..
//			throw new Exception("Not enough blocks in chain to make valid MMR Proofs..");
//		}
//		
//		//Get the main Wallet
//		Wallet walletdb = MinimaDB.getDB().getWallet();
//		
//		//Create a list of the required signatures
//		ArrayList<String> reqsigs = new ArrayList<>();
//		
//		//Add the MMR proofs for the coins..
//		for(Coin input : inputs) {
//			
//			//Get the proof..
//			MMRProof proof = mmrnode.getMMR().getProofToPeak(input.getMMREntryNumber());
//			
//			//Create the CoinProof..
//			CoinProof cp = new CoinProof(input, proof);
//			
//			//Add it to the witness data
//			witness.addCoinProof(cp);
//			
//			//Add the script proofs
//			String scraddress 	= input.getAddress().to0xString();
//			KeyRow keyrow 		= walletdb.getKeysRowFromAddress(scraddress); 
//			if(keyrow == null) {
//				throw new Exception("SERIOUS ERROR script missing for simple address : "+scraddress);
//			}
//			
//			ScriptProof pscr = new ScriptProof(keyrow.getScript());
//			witness.addScript(pscr);
//			
//			//Add this address to the list we need to sign as..
//			String priv = keyrow.getPrivateKey();
//			if(!reqsigs.contains(priv)) {
//				reqsigs.add(priv);
//			}
//		}
//		
//		//Calculate the TransactionID..
//		MiniData transid = Crypto.getInstance().hashObject(zTransaction);
//		
//		//Now that we have constructed the transaction - lets sign it..
//		for(String priv : reqsigs) {
//
//			//Use the wallet..
//			Signature signature = walletdb.sign(priv, transid);
//			
//			//Add it..
//			witness.addSignature(signature);
//		}
//		
//		return witness;
//	}
}