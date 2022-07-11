package discord.yutarpg.work;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import priv.key.Bouncycastle_Secp256k1;
import priv.key.Secp256k1;

/**
 * Hello world!
 *
 */
public class App extends ListenerAdapter {
	static JDABuilder jdabuilder = new JDABuilder();
	static JDA jda;
	static Socket node;
	static HashMap<String,BigInteger>id_priv=new HashMap<String,BigInteger>();
	static HashMap<String, String> id_pub = new HashMap<String,String>();
	static int day = LocalDateTime.now().getDayOfMonth();
	static HashMap<String, BigDecimal> role_income = new HashMap<String,BigDecimal>();
	static BigDecimal netwall = new BigDecimal(10000);
	static HashMap<String,BigDecimal> utxo=new HashMap<String,BigDecimal>();
	static HashMap<String,Entry<MessageChannel,String>> transactionHashes=new HashMap<String,Entry<MessageChannel,String>>();
	static long cacheTimeStampMilli=5000;
	/**
	 * 0:確認中
	 * １:ok
	 * 2:no
	 */

	public static void main(String[] args) {
		Socket so = new Socket();
		InetSocketAddress endpoint_ = new InetSocketAddress("localhost", 65261);
		try {
			so.connect(endpoint_, 1024);
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		node = so;
		new Receiver().start();
		try {
			node.getOutputStream().write("balances~\r\n".getBytes());
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		role_income.put("総本部長", new BigDecimal(0.07));
		role_income.put("顧問", new BigDecimal(0.07));
		role_income.put("総本部-執行役員", new BigDecimal(0.06));
		role_income.put("本部長", new BigDecimal(0.05));
		role_income.put("本部-執行役員", new BigDecimal(0.04));
		role_income.put("部長", new BigDecimal(0.03));
		role_income.put("課長", new BigDecimal(0.02));
		role_income.put("職員", new BigDecimal(0.01));
		/*role_income.put("運営OB", 300);
		role_income.put("常連", 200);
		role_income.put("来賓", 100);*/
		loadWallets();
		try {
			System.out.println("Please input DISCORD TOKEN!");
			Scanner sc=new Scanner(System.in);
			jda = jdabuilder.setToken(sc.nextLine())
					.addEventListeners(new App()).setActivity(Activity.playing("%help で取引を開始しましょう！")).build();
			jda.awaitReady();
			sc.close();
		} catch (LoginException | InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		new Diary().start();

	}

	String src_sign = null;

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		System.out.println("受け取り。");
		File f = new File("wallet.txt");
		MessageChannel channel = event.getChannel();
		Message msg = event.getMessage();
		if (msg.getContentRaw().startsWith("%create")) {
			BufferedReader fr=null;
			try {
				fr = new BufferedReader(new FileReader(f));
				FileWriter fw=new FileWriter(f,true);
				String s=null;
				boolean isWrited=false;
				while((s=fr.readLine())!=null) {
					if(s.split("~")[0].equals(msg.getAuthor().getId())) {
						isWrited=true;
					}
				}
				System.out.println("???0");
				if(!isWrited) {
					Secp256k1 g = new Secp256k1();
					BigInteger[] pubK1 = new BigInteger[2];
					Random rn = new Random();
					BigInteger   priK = new BigInteger(255,rn);
					pubK1 = g.multiply_G(priK);
					byte[] priv=priK.toByteArray();
					fw.append(msg.getAuthor().getId()+"~"+new BigInteger(priv).toString(16)+"\r\n");
					loadWallets();
					channel.sendMessage("さいふをつくったよ！").queue();
				}else {
					channel.sendMessage("もう作ってあるよ！"+id_pub.get(msg.getAuthor().getId())).queue();
				}
			} catch (Exception e) {
				// TODO 自動生成された catch ブロック
				System.out.println("???");
				e.printStackTrace();
			}
		}else if (msg.getContentRaw().startsWith("%balance")) {
			if(System.currentTimeMillis()-cacheTimeStampMilli>=5000) {
				channel.sendMessage("残額の キャッシュを更新するね").queue();
				try {
					node.getOutputStream().write("balances~\r\n".getBytes());
					Thread.sleep(900);
				} catch (Exception e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				}
			}
			try {
				String pub=id_pub.get(msg.getAuthor().getId()).split("0x0a")[0];
				BigDecimal balance=utxo.get(pub);
				if(balance==null) {
					channel.sendMessage("残額が見つかったよ！残額は、\r\n"+0+"GWCだよ！").queue();
				}else {
					channel.sendMessage("残額が見つかったよ！残額は、\r\n"+balance+"GWCだよ！").queue();
				}
			}catch(Exception e) {
				channel.sendMessage("さいふがなかったよ\r\nマイニングができてないだけかも\r\nさいふは%createコマンドでつくれるよ！").queue();
			}
		} else if (msg.getContentRaw().equals("%addr") || msg.getContentRaw().equals("%address")) {
			channel.sendMessage("きみのさいふは" + id_pub.get(event.getAuthor().getId()) + "だよ！").queue();
		} else if (msg.getContentRaw().equals("%help")) {
			String s = "コマンド:gear:：\r\n" +
					"├`%help : ヘルプ`\r\n" +
					"├`%create : 財布を作るよ！`\r\n" +
					"├`%balance : おかねの　のこりだよ！`\r\n" +
					"│　└`%balance <ID> : ほかのフレンズ　の残高だよ！`\r\n" +
					"├`%addr : きみの　アドレスだよ！`\r\n" +
					"│　└`%address : うえとおんなじ！`\r\n" +
					"├`%trade <取引相手(アドレスかメンションを使えるよ)> <amount> [fee] : とりひきを　できるよ！！`\r\n" +
					"└　├`例 : %trade @Gosh#5490 40 0.5`\r\n" +
					"　　└手数料は省略できるよ！*省略したら0.1が指定されたことになるよ*"+
					"運営への報:moneybag:：\r\n" +
					"└やくわりを　もってる　ひとには　１にちに１かい　コインが　ネットワークの　ウォレットから　しはらわれるよ！\r\n" +
					"作者：狩りごっこが苦手なフレンズ";
			channel.sendMessage(s).queue();
		} else if (msg.getContentRaw().startsWith("%trade")) {
			try {
				long time = System.currentTimeMillis();
				String[] cmd = msg.getContentRaw().split(" ");

				String S_1 = null;
				String addr = null;
				try {
					if(cmd[1].contains("!")) {
						S_1 = cmd[1].split("<@!")[1].split(">")[0];
						addr = id_pub.get(S_1);
					}else if(cmd[1].contains("&")){
						S_1 = cmd[1].split("<@&")[1].split(">")[0];
						addr = id_pub.get(S_1);
					}else if(cmd[1].contains("<@")) {
						S_1=cmd[1].split("<@")[1];
						S_1=S_1.substring(0,S_1.length()-1);
						System.out.println("parsed : "+S_1);
						addr = id_pub.get(S_1);
					}else if(id_pub.containsKey(cmd[1])) {
						addr = id_pub.get(cmd[1]);
					} else if(cmd[1].matches("[0-9a-fA-F]*0x0a[0-9a-fA-F]*")){
						System.out.print("パターンにマッチした。");
						addr = cmd[1];
					}else {
						channel.sendMessage("あてさき　が　わからないよ！").queue();
						return;
					}
					if (addr == null) {
						channel.sendMessage("あてさきのひと　が　さいふをもってないかも").queue();
						return;
					}
				} catch (Exception e) {
					channel.sendMessage("あてさきをしていしてね").queue();
					return;
				}

				BigDecimal fee = new BigDecimal("0.1");
				if (cmd.length<3) {
					fee = new BigDecimal(cmd[3]);
				}else {
					channel.sendMessage("てすうりょうを　デフォルトの0.1使うよ！").queue();
				}
				try {
					String address=id_pub.get(msg.getAuthor().getId());
					BigDecimal balance=utxo.get(address.split("0x0a")[0]);
					if(balance==null) {
						channel.sendMessage("残額がないよ！").queue();
						return;
					}
					if(cmd.length<2) {
						channel.sendMessage("引数が少ないよ！").queue();
						return;
					}
					if(balance.compareTo(fee.add(new BigDecimal(cmd[2])))<0) {
						channel.sendMessage("お金が足りないよ！").queue();
						return;
					}

					//サインの原文作成：@なし
					src_sign = id_pub.get(event.getAuthor().getId()) 
							+addr.replace("0x0a", "0x0b") + "0x0c" + new BigDecimal(cmd[2])
							+fee
							+time;
					BigInteger[] sign = sign(event.getAuthor().getId(),
							(hash(src_sign).getBytes()));
					String transaction_sum = id_pub.get(event.getAuthor().getId()) +
							"@" + addr.replace("0x0a", "0x0b") + "0x0c" + new BigDecimal(cmd[2]) +
							"@" + fee +
							"@" + time + "@" + sign[0].toString(16) + "0x0a" + sign[1].toString(16);
					System.out.println(src_sign + "\r\n" + transaction_sum);
					String kakunin = "`送信先→" + addr + "\r\n"
							+ "数量→" + new BigDecimal(cmd[2]) + "`\r\n"
							+"そうしんしたよ!";
					channel.sendMessage(kakunin).queue();
					transactionHashes.put(sign[0].toString(16),new SimpleEntry<MessageChannel,String>(channel, msg.getAuthor().getId()));
					node.getOutputStream().write(("transaction~"+transaction_sum+"\r\n").getBytes());
				} catch (Exception e) {
					channel.sendMessage("だめだった...エラーが発生したよ..\r\n%trade <取引相手(アドレスかメンションを使えるよ)> <amount> [fee]に従ってね").queue();
					e.printStackTrace();
				}
			}catch(Exception e) {e.printStackTrace();}
		} else if (msg.getContentRaw().startsWith("%check")) {
			channel.sendMessage("node:"+((node.isClosed())?"ERROR":"OK!")).queue();

		}
	}
	static void loadWallets() {
		File f = new File("wallet.txt");
		try {
			try {
				BufferedReader fr = new BufferedReader(new FileReader(f));
				for (; f.exists();) {
					BigInteger[] pub = new BigInteger[2];
					String line = fr.readLine();
					if (line == null)
						break;
					BigInteger b = new BigInteger(line.split("~")[1], 16);
					id_priv.put(line.split("~")[0],b);
					Secp256k1 g = new Secp256k1();
					pub = g.multiply_G(b);
					id_pub.put(line.split("~")[0], pub[0].toString(16) + "0x0a" + pub[1].toString(16));
				}
				fr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	static BigInteger[] sign(String s, byte[] hash) {
		//署名用の乱数
		byte[] priv;
		BigInteger priK = id_priv.get(s);
		priv = priK.toByteArray();
		Random rn = new Random();
		byte[] ran = new BigInteger(255, rn).toByteArray();
		BigInteger[] sig = Bouncycastle_Secp256k1.sig(hash, priv, ran);
		return sig;
	}

	public static String hash(String arg) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		byte[] hashInBytes = md.digest(arg.getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		for (byte b : hashInBytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}
