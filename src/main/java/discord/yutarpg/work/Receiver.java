package discord.yutarpg.work;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

public class Receiver extends Thread{
	@Override
	public void run() {
		System.out.println("Started receiver");
		InputStream sock_in = null;
		try {
			sock_in =App.node.getInputStream();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		InputStreamReader sock_is = new InputStreamReader(sock_in);
		BufferedReader sock_br = new BufferedReader(sock_is);
		String s=null;
		try {
			while ((s=sock_br.readLine())!=null) {
				System.out.println(s);
				if (s.startsWith("balance~")) {
					String utxo=s.split("~")[1];
					String[] arg=utxo.split(",");
					App.utxo.put(arg[0], new BigDecimal(arg[1]));
					System.out.println(arg[0]+","+arg[1]);
				}
			}
			System.out.println("thread-Receiver終了");
		}catch(Exception e) {e.printStackTrace();}
	};
}
