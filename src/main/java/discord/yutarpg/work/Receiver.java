package discord.yutarpg.work;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map.Entry;

import net.dv8tion.jda.api.entities.MessageChannel;

public class Receiver extends Thread{
	@Override
	public void run() {
		System.out.println("Started receiver");
		InputStream sock_in = null;
		try {
			sock_in =App.node.getInputStream();
			InputStreamReader sock_is = new InputStreamReader(sock_in);
			BufferedReader sock_br = new BufferedReader(sock_is);
			String s=null;

			while ((s=sock_br.readLine())!=null) {
				try {
					System.out.println(s);
					if (s.startsWith("balance~")) {
						String utxo=s.split("~")[1];
						String[] arg=utxo.split(",");
						App.utxo.put(arg[0], new BigDecimal(arg[1]));
						System.out.println(arg[0]+","+arg[1]);
						App.cacheTimeStampMilli=System.currentTimeMillis();
					}else if(s.startsWith("ok~")) {
						String sign=s.split("~")[1];
						Entry<MessageChannel,String> entry=App.transactionHashes.get(sign);
						entry.getKey().sendMessage("<@"+entry.getValue()+">さんの取引が認証されたよ！\r\nでも残額はマイニングされるまで増えないよ！").queue();
					}else if(s.startsWith("reject~")) {
						String sign=s.split("~")[1];
						Entry<MessageChannel,String> entry=App.transactionHashes.get(sign);
						entry.getKey().sendMessage("<@"+entry.getValue()+">さんの取引がなぜかネットワークのノードに拒否されたよ！").queue();
						App.transactionHashes.remove(sign);
					}else if(s.startsWith("block~")) {
						ArrayList<String> contains=new ArrayList<String>();
						for(Entry<String,Entry<MessageChannel,String>> entry:App.transactionHashes.entrySet()) {
							if(s.contains(entry.getKey())) {
								contains.add(entry.getKey());
							}
						}
						for(String str:contains) {
							App.transactionHashes.get(str).getKey().sendMessage("<@"+App.transactionHashes.get(str).getValue()+">さんの取引を含んだブロックがマイニングされました").queue();
						}
					}
				}catch(Exception e) {e.printStackTrace();}
			}
			System.out.println("thread-Receiver終了");
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	};
}
