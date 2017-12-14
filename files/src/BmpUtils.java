import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class BmpUtils {

	public static void main(String[] args) throws Exception {
		
		Scanner scanMenu= new Scanner(System.in);
		System.out.print("Digite o diretorio + nome do arquivo (Ex.: files/ttt.bmp):");
		String fileS = scanMenu.nextLine();
		RandomAccessFile img =  new RandomAccessFile(fileS, "rw");
		RandomAccessFile imgF =  new RandomAccessFile(fileS + "-new.bmp", "rw");
		
		img.seek(0x12);
		
		byte [] width = new byte [4];
		byte [] height = new byte [4];
		img.read(width);
		img.read(height);
		
		int headLgt= 0x36;
		img.seek(0);
		byte [] head = new byte [headLgt];
		img.read(head);
		
		imgF.write(head);
		
		width = inverte(width);
		height = inverte(height);
		
		int w = ByteBuffer.wrap(width).getInt();
		int h = ByteBuffer.wrap(height).getInt();
		int wBytes= w*3;
		
		img.seek(headLgt);
		
		
	
		
		boolean end = false;
		while(end != true) {
			int a = 0;
			System.out.print("Escolha a opcao:\n"
							 + "1 - Espelhar\n"
							 + "2 - Inverter\n"
							 + "3 - Escala Cinza\n"
							 + "4 - Rotacionar\n"
							 + "5 - Dobrar\n"
							 + "6 - Sair\n"
							 + "1-6:");
			a = scanMenu.nextInt();
			switch(a) {
			case 1: img = espelhar(img, imgF, wBytes, h, headLgt); break; 
			case 2: img = inverter(img, imgF, wBytes, h); break; 
			case 3: img = escalaCinza(img, imgF, wBytes, h, headLgt); break;
			case 4: img = rotacionar(img,imgF, h, headLgt, w, width, height); break;
			case 5: img = dobrar(img,imgF, h, headLgt, w, width, height); break;
			case 6: end =true;
			}
		}
		
		img.close();
		imgF.close();
		scanMenu.close();
		
	}
	
	public static byte[] inverte(byte [] a) {
		
		int j= a.length;
		byte aux[] = new byte[a.length];
		j--;
		for (int i = 0; i < a.length; i++) {
			aux[j] = a[i];
			j--;
		}
		return aux;
	}
	
	public static byte[] invertePixel(byte [] a) {
		int j= a.length;
		byte aux[] = new byte[a.length];
		j--;
		for (int i = 0; i < a.length;) {
			aux[j-2] = a[i];
			aux[j-1] = a[i+1];
			aux[j] = a[i+2];
			j-= 3;
			i+=3;			
		}
		
		return aux;
	}
	
	public static RandomAccessFile espelhar(RandomAccessFile img, 
			RandomAccessFile imgF, int wBytes, int h, int head) throws Exception{
		byte [] aux = new byte [wBytes];
		img.seek(head);
		for(int i = 0; i<h; i++) {
			img.read(aux);
			
			aux = invertePixel(aux);
			
			imgF.write(aux);
			
		}
		return imgF;
	}
	
	public static RandomAccessFile inverter(RandomAccessFile img,
			RandomAccessFile imgF, int wBytes, int h) throws Exception{
		byte [] aux = new byte [wBytes];
		for(int i = 0; i<h; i++) {
			int l = i + 1;
			img.seek(img.length() - l*wBytes);
			img.read(aux);
			aux = invertePixel(aux);
			imgF.write(aux);	
		}
		return imgF;
	}
	
	public static RandomAccessFile escalaCinza(RandomAccessFile img,
			RandomAccessFile imgF, int wBytes, int h, int head) throws Exception{
		byte [] aux = new byte [wBytes];
		img.seek(head);
		for(int i = 0; i<h; i++) {
			img.read(aux);
			for(int j= 0; j<aux.length;) {
			int a = (int) aux[j];
			int b = (int) aux[j+1];
			int c = (int) aux[j+2];
			
			int m= (int) ((0.58*a) + (0.17*b) + (0.8*c));
				
			aux[j] = (byte) m;
			aux[j+1] = (byte) m;
			aux[j+2] = (byte) m;
				
			j+=3;
			}
			imgF.write(aux);
		}
		return imgF;
	}
	
	public static RandomAccessFile rotacionar(RandomAccessFile img, RandomAccessFile imgF, 
			int h, int header, int w, byte[] width, byte[] height) throws Exception{
		int newBWd = h*3;
		byte [] aux = new byte [newBWd];
		
		int pointer = 0;
		for(int i= 0; i<w; i++) {
			int k =0;
			for(int j=h; j>0; j-- ) {
				pointer = header + (j-1)*(w*3);
				pointer += i*3;
				img.seek(pointer);
				
				aux[k] = img.readByte();
				
				aux[k+1] = img.readByte();

				aux[k+2] = img.readByte();
				
				k+=3;
			}

			imgF.seek(imgF.length());
			imgF.write(aux);
		}
		
		imgF.seek(0x12);
		
		imgF.write(inverte(height));
		imgF.seek(0x16);
		imgF.write(inverte(width));
		return imgF;
			
		}
		
public static RandomAccessFile dobrar(RandomAccessFile img, RandomAccessFile imgF,
		int h, int header, int w, byte[] width, byte[] height) throws Exception{
			
		int a = ByteBuffer.wrap(width).getInt();
		String aS = Integer.toHexString(a*2);
		a = (int) Long.parseLong(aS, 16);
		ByteBuffer wBB = ByteBuffer.allocate(4).putInt(a);
		width = wBB.array();	
		int b = ByteBuffer.wrap(height).getInt();
		String bS = Integer.toHexString(b*2);
		b = (int) Long.parseLong(bS, 16);
		ByteBuffer hBB = ByteBuffer.allocate(4).putInt(b);
		height = hBB.array();
		
		imgF.seek(0x12);
		imgF.write(inverte(width));
		imgF.seek(0x16);
		imgF.write(inverte(height));
		imgF.seek(header);
		long position = header;
		byte [] aux = new byte [w*3];
		byte [] auxD =  new byte [(aux.length)*2];
		for(int i= 0; i<h; i++){
			img.seek(position);
			img.read(aux);
			int k=0;
			for (int j = 0; j < aux.length;) {	
				auxD[k] = aux[j];
				auxD[k+1] = aux[j+1];
				auxD[k+2] = aux[j+2];
				auxD[k+3] = aux[j];
				auxD[k+4] = aux[j+1];
				auxD[k+5] = aux[j+2];
				
				j+=3;
				k+=6;
				}
			
			imgF.write(auxD);
			imgF.write(auxD);
			position = img.getFilePointer();
		}	
		return imgF;	
		}
		
		

	
	
}
