package discord.yutarpg.work;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Receiver extends Thread{
	Socket s=null;
	Receiver(Socket s){
		this.s=s;
	}
	@Override
	public void run() {
		InputStream sock_in = null;
		try {
			sock_in =App.node.getInputStream();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		InputStreamReader sock_is = new InputStreamReader(sock_in);
		BufferedReader sock_br = new BufferedReader(sock_is);

		for (;;) {
			try {
				String s = sock_br.readLine();
				System.out.println(s);
				if (s.startsWith("balance~")) {

				}
			}catch(Exception e) {e.printStackTrace();}
		}
	};
}
