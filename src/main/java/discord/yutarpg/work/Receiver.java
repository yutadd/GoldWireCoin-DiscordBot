package discord.yutarpg.work;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class Receiver extends Thread{
	Socket s=null;
Receiver(Socket s){
	this.s=s;
}
@Override
public void run() {
	InputStream sock_in = null;
	try {
		sock_in = node.getInputStream();
	} catch (IOException e) {
		// TODO 自動生成された catch ブロック
		e.printStackTrace();
	}
	InputStreamReader sock_is = new InputStreamReader(sock_in);
	BufferedReader sock_br = new BufferedReader(sock_is);

	for (;;) {
		boolean mitsukatta = false;
		try {
			String s = sock_br.readLine();
			System.out.println(s);
			if (s.startsWith("balance~")) {
				for (MessageChannel mc : jda.getTextChannelsByName(s.split("~")[1], false)) {
					mc.sendMessage(s.split("~")[2] + "がもってる　おかねは" + s.split("~")[3] + "だよ！").queue();
					mitsukatta = true;
				}
				if (!mitsukatta) {
					try {
						User user = jda.getUserByTag(s.split("~")[1]);
						PrivateChannel pc = user.openPrivateChannel().complete();
						pc.sendMessage(s.split("~")[2] + "がもってる　おかねは" + s.split("~")[3] + "だよ！").queue();
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("チャンネル、DMが見つかりませんでした。");
					}
				}
			} else if (s.endsWith("~ok")) {
				System.out.println("mining size : " + mining.size());
				System.out.println(s);
				MessageAction ma = mining.get(s.split("~")[0]).editMessage(
						"`そうしん　したよ！[ok]\r\n　　＼\r\n　　とりひきが　せいとうだと　みとめられたよ[ok]\r\n　　　　　＼\r\n　　　　まいにんぐ　してるよ！`");
				mining.put(s.split("~")[0], ma.complete());
			} else if (s.endsWith("~denny")) {
				try {
					System.out.println("mining size : " + mining.size());
					System.out.println(s);
					if (mining.containsKey(s.split("~")[0])) {
						mining.get(s.split("~")[0]).editMessage(
								"`そうしん　したよ！[ok]\r\n　　＼\r\n　　とりひきが　みとめられなかったよ[X]\r\n　　　　　＼\r\n　　　　とりひきに しっぱいしたよ...`")
						.queue();
					}
				} catch (Exception e) {
					System.out.println("");
				}
				mining.clear();
			} else if (s.endsWith("~mined")) {
				String st = s.split("~")[0];
				String[] hashs = st.split("0x0d");
				for (String hash : hashs) {
					if (mining.containsKey(hash)) {
						mining.get(hash).editMessage(
								"`そうしん　したよ！[ok]\r\n　　＼\r\n　　とりひきが　せいとうだと　みとめられたよ[ok]\r\n　　　　　＼\r\n　　　　とりひきが　かんりょうしたよ！[ok]`")
						.queue();
						mining.remove(hash);
					}
				}
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}
}
