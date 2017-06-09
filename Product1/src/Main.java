
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
//import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.video.ConverterFactory;
import org.opencv.videoio.VideoCapture;

import static org.opencv.video.Video.createBackgroundSubtractorMOG2;

public class Main extends Thread {
	
	//private static final String outputFilename = "//home//nuc//Desktop//videos//";
	//private static final String outputFilename = "//home//odroid//Desktop//videos//";
	private static final String outputFilename = "//Users//mithileshhinge//Desktop//videos//";
	public static IMediaWriter writer;
	public static boolean store = false;
	public static long startTime;
	public static Date dNow;
	public static SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd'at'hh_mm_ssa");
	public static boolean writer_close = false;
	public static String store_name;
	public static String store_file_name;
	static OutputStream out;
	
	private static boolean vid_small = true;
	
	public static final String outputFilename4android = "/Users//mithileshhinge//Desktop//videos4android//";
	public static IMediaWriter writer4android;
	public static boolean writer_close4android = false;
	public static String store_name4android;
	public static boolean once =false;
	static long timeNow1, timeNow2;
	static long time3, time4;
	public static int j = 0;
	
	public static int myNotifId = 1;
	
	public static Process proc;
	//Disable auto focus of camera through terminal
	
	public static int framesRead = 0;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	/*static {
		System.loadLibrary("opencv_java248");
	}*/
	
	public static void main(String[] args) throws IOException {
		
		SendingFrame sendingFrame = new SendingFrame();
		sendingFrame.start();

		SendingVideo sendingVideo = new SendingVideo();
		sendingVideo.start();
	
		// sending notification to android
		NotificationThread t2 = new NotificationThread();
		t2.start();
		
		//sending mail
		SendMail t3 = new SendMail();
		t3.start();

		/*try{
			proc=Runtime.getRuntime().exec("sh cam");
			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String s;
			while ((s = br.readLine())!= null)
			System.out.println(s);
			proc.waitFor();
			System.out.println("exit:"+proc.exitValue());
			proc.destroy();
			
		}catch (Exception e1){
			e1.printStackTrace();
		}*/
		
		VideoCapture capture = new VideoCapture(0);

		BackgroundSubtractorMOG2 backgroundSubtractorMOG = createBackgroundSubtractorMOG2(333, 16, false);
		
		if (!capture.isOpened()) {
			System.out.println("Error - cannot open camera!");
			return;
		}

		while (true) {
			timeNow1 = System.currentTimeMillis();
			Mat camImage = new Mat();
			capture.read(camImage);

			Mat camImageLow = new Mat();
			Imgproc.pyrDown(camImage, camImageLow);

			BufferedImage unprocessed_image = MatToBufferedImage(camImageLow); // 265 ms

			sendingFrame.frame = unprocessed_image;

			if (camImage.empty()) {
				System.out.println(" --(!) No captured frame -- Break!");
				continue;
			}
			
			// Background subtraction method
			Mat fgMask = new Mat();
			
			if (j == 1) {
				backgroundSubtractorMOG.apply(camImage, fgMask, -1);
				j = 0;
			}else backgroundSubtractorMOG.apply(camImage, fgMask, 0);
			
			
			byte[] buff = new byte[(int) (fgMask.total() * fgMask.channels())];
			fgMask.get(0, 0, buff);

			int blackCount = 0;
			for (int i = 0; i < buff.length; i++) {
				if (buff[i] == 0) {
					blackCount++;
				}
			}
			System.out.println("" + (100 * blackCount / buff.length) + "%");
			
			final int blackCountPercent = 100*blackCount/buff.length;
			
			Mat output = new Mat();
			camImage.copyTo(output, fgMask);

			if (blackCountPercent < 97 && framesRead > 333) {
				
				// storing video to outputfilename
				if (store == true) {
					time3 = System.currentTimeMillis();
					time3 = time4;
					store_name = outputFilename + ft.format(dNow) + ".mp4";
					store_name4android = outputFilename4android + ft.format(dNow) + ".mp4";
					store_file_name = ft.format(dNow);
					writer = ToolFactory.makeWriter(store_name);
					writer4android = ToolFactory.makeWriter(store_name4android);
					writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 640, 480);
					writer4android.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 640, 480);
					startTime = System.nanoTime();
					writer_close = true;
					t2.p=1;
					t2.myNotifId = myNotifId;
					t2.sendNotif = true;
				}
				
				store = false;
				BufferedImage camimg = MatToBufferedImage(camImage);
				// encode the image to stream #0
				writer.encodeVideo(0, camimg, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
				if(time4-time3<10000){
				writer4android.encodeVideo(0, camimg, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
				}
				if (time4-time3>10000 && !once)
				{
					writer_close4android= true;
					SendMail.sendmail_notif=true;
					vid_small = false;
					once=true;
				}
				
			} else {
				dNow = new Date();
				// System.out.println("Current Date: " + ft.format(dNow));
				store = true;
				if (writer_close) {
					writer.close();
					writer_close = false;
					once = false;
					SendMail.sendmail_vdo = true;
					if(vid_small && time4-time3>4000){
						writer_close4android= true;
						//SendMail.sendmail_notif=true;
						System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ SMALL VIDEO @@@@@@@@@@@@@@@@@@@@@@@@");
					}
					vid_small = true;
				}
				
					
				backgroundSubtractorMOG.apply(camImage, fgMask, -1);
			}
			if (writer_close4android) {
				writer4android.close();
				writer_close4android = false;
				t2.p =2;
				t2.myNotifId = myNotifId;
				t2.sendNotif = true;
				sendingVideo.notifId2filepaths.put(new Integer(myNotifId), store_name4android);
				myNotifId++;
			}
			
			if (framesRead < 350) {
				framesRead++;
			}
			time4 = System.currentTimeMillis();
			timeNow2 = System.currentTimeMillis();
			System.out.println(timeNow2 - timeNow1);
			System.out.println("frmes_read" + framesRead);
			timeNow1 = timeNow2;
		}
	}

	private static BufferedImage MatToBufferedImage(Mat frame) {
		int type = 0;
		if (frame.channels() == 1) {
			type = BufferedImage.TYPE_BYTE_GRAY;
		} else if (frame.channels() == 3) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
		WritableRaster raster = image.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		frame.get(0, 0, data);

		return image;
	}

}