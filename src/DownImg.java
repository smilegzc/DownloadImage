import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//http://p.gmgm8.wang/uploadfile/2016/0318/20/255.jpg

class DownImg implements Runnable{
	private final URL imgUrl;
	private final String imgName;
	private final String pathName;
	private static double progress = 0;
	private static int numImg = 0;
	
	public static void main(String[] args) {
		if(args[0].equals("-h") || args[0].equals("-help") || args[0].equals("help")) {
			System.out.println("参数：[图片url] [图片数量] [文件夹名称(文件夹位于D盘根目录下)]");
			System.exit(0);
		}
		if(args.length != 3) {
			System.out.println("参数输入有误！请使用-h参数查看帮助信息");
			System.exit(0);
		}
		String url = args[0];
		numImg = Integer.valueOf(args[1]);
		
		String[] urlTemp = url.split("/\\d+\\.");
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		for(int i = 1; i <= numImg; i++) {
			String temp;
			if(i >= 10) {
				temp = String.valueOf(i);
			} else {
				temp = "0" + i;
			}
			try {
				URL imgUrl = new URL(urlTemp[0] + "/" + temp + "." + urlTemp[1]);
				String imgName = temp + "." + urlTemp[1];
				executorService.submit(new DownImg(imgUrl, imgName, args[2]));
			} catch(MalformedURLException e) {
				e.printStackTrace();
			}
		}
		executorService.shutdown();
		System.out.println("正在下载，请稍候。。。");
	}
	
	private DownImg(URL imgUrl, String imgName, String pathName) {
		this.imgUrl = imgUrl;
		this.imgName = imgName;
		this.pathName = pathName;
	}
	
	private void getImg(URL imgUrl, String imgName, String pathName) {
		HttpURLConnection urlCon = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		try {
			urlCon = (HttpURLConnection) imgUrl.openConnection();
			urlCon.connect();
			bis = new BufferedInputStream(urlCon.getInputStream());
			File file = new File("D:/" + pathName);
			if(!file.isDirectory()) {
				file.mkdir();
			}
			fos = new FileOutputStream(new File(file, imgName));
			
			byte[] imgBuff = new byte[1024*1024];
			int size;
			while((size = bis.read(imgBuff)) != -1) {
				fos.write(imgBuff, 0, size);
			}
			fos.flush();
			
			progress += 100.00/numImg;
			String pro = String.format("%.2f", progress);
			if(progress < 10) {
				pro = "0" + pro;
			}
			System.out.print(pro + "%" + "\b\b\b\b\b\b");
		} catch(IOException e) {
			System.out.println(imgName + "连接超时,正在重试");
			getImg(imgUrl, imgName, pathName);
		} finally {
			try {
				if(fos != null) {
					fos.close();
				}
				if(bis != null) {
					bis.close();
				}
				urlCon.disconnect();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void run() {
		getImg(imgUrl, imgName, pathName);
	}
}