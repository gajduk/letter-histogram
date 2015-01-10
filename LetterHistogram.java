import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

class NIOAsynchFileReader implements CompletionHandler<Integer, AsynchronousFileChannel> {
	ByteBuffer buffer = ByteBuffer.allocate(50000000);
	LetterHistogram hist;
	int pos = 0;
	
	public NIOAsynchFileReader(LetterHistogram hist, AsynchronousFileChannel file_channel) {
		this.hist = hist;
		file_channel.read(buffer, 0,file_channel,this);
	}
	
	@Override
	public void completed(Integer result,
			AsynchronousFileChannel attachment) {
		 buffer.rewind();
		 while ( buffer.hasRemaining() ) {
		  	char c = (char) Character.toLowerCase(buffer.get());
		  	if ( c >= 'a' && c <= 'z' ) 
					++hist.count[c-'a'];
		 }
		 buffer.clear();
		 pos += result;
		 if ( result != -1 ) {
			 if ( hist.refresh() )
				 attachment.read(buffer, pos, attachment, this);
		 }
		 else {
			 hist.status.setForeground(Color.black);
			 hist.status.setText("Successfully read file.");
			 hist.refresh();
		 }
	}
	@Override
	public void failed(Throwable exc,
			AsynchronousFileChannel attachment) {
		hist.status.setForeground(Color.red);
		hist.status.setText("Problem reading file.");
	}
}

public class LetterHistogram extends JFrame {
	
	private static final long serialVersionUID = -5817103053693053435L;
	final JFileChooser fc;
	JTextField file_name,status;
	int count[];// = { 10 , 23 , 12 , 9 , 0 , 8 , 8 , 1 , 2 , 3  , 1,2  , 3, 3, 4, 2,5 ,6 ,6 ,7 , 2 , 86 ,45 , 86 , 9 , 23 }; 
	
	
	public LetterHistogram ( ) {
		fc = new JFileChooser();
		JButton choose_file = new JButton("Choose a file");
		choose_file.setMargin(new Insets(2, 2, 2, 2));
		file_name = new JTextField();
		status = new JTextField();
		file_name.setEditable(false);
		status.setEditable(false);
		status.setText("Ready...");
		file_name.setText("Click the button to choose a file.                                         ");
		choose_file.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fc.showOpenDialog(LetterHistogram.this);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            calcualteHistogram(file.getPath());
		            file_name.setText(file.getPath());
		            repaint();
		        } 
			}

		});
		JButton how_to = new JButton("How to?");
		how_to.setMargin(new Insets(2, 2, 2, 2));
		how_to.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(LetterHistogram.this,
					    "Click on choose file to open a file browse dialog and choose a text file.\n"+
						"Wait a minute for the file to load properly.\n"+
					    "The bar colors correpond to letter frequency:\n"+
					    "\t red - most frequent\n"+
					    "\t blue - least frequent\n"+
					    "The application uses the NIO features of Java, so feel free to play with large files.\n"+
					    "Have fun!\n"+
					    "Made by Andrej Gajduk.",
					    "How to use?",
					    JOptionPane.PLAIN_MESSAGE);
			}
			
		});
		this.setLayout(new BorderLayout());
		JPanel top_panel = new JPanel();
		top_panel.add(how_to);
		top_panel.add(choose_file);
		top_panel.add(file_name);
		this.add(top_panel,BorderLayout.NORTH);
		this.add(status,BorderLayout.SOUTH);
		this.setTitle("Letter Histogram");
		this.setSize(520,400);
		this.setVisible(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	protected void calcualteHistogram(String name) {
		count = new int[26];
		Path file = Paths.get(name);
		
		try {
			status.setForeground(Color.black);
			status.setText("Reading file, please stand by");
			repaint();
			AsynchronousFileChannel channel =
			AsynchronousFileChannel.open(file);
			new NIOAsynchFileReader(this,channel);
		} catch (IOException e) {
			status.setText("Error reading file:"+name);
			status.setForeground(Color.red);
			count = null;
		}
		
	}
	
	boolean refresh() {
		 if ( ! checkCount() ) {
			 	status.setForeground(Color.red);
			 	status.setText("File does not contain text.");
			 	return false;
		 }
		 repaint();
		 return true;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		int plot_left = 100;
		int plot_top = 310;
		int plot_height = 230;
		int plot_width = 320;
		g2d.setStroke(new BasicStroke(2));
		g.drawLine(plot_left,plot_top,plot_left,plot_top-plot_height);
		g.drawLine(plot_left,plot_top,plot_left+plot_width,plot_top);
		if ( ! checkCount() ) {
			count = new int[26];
		}
			double max = 1;
			for ( int i = 0 ; i < count.length ; ++i ) {
				max = Math.max(max,count[i]);
			}
			double hpo = plot_height*0.85/max;
			g2d.setStroke(new BasicStroke(1));
			double t = plot_height / 10;
			for ( int i = 0 ; i < 10 ; ++i ) {
				g.drawLine(plot_left,(int)( plot_top-t*i),plot_left+plot_width,(int)(plot_top-t*i));
				String tt = Integer.toString((int)(t*i*1.0/hpo));
				if ( i != 0 )
					g.drawChars(tt.toCharArray(),0,tt.length(),plot_left-8*tt.length()-6,3+(int)(plot_top-t*i));
			}
			for ( int i = 0 ; i < count.length ; ++i ) {
				int h = (int) (hpo*count[i]);
				int weight =  (int)((count[i]/max*255));
				int red = Math.min(weight,255);
				int blue = Math.max(0,255-weight);
				g.setColor(new Color(red,0,blue));
				g.fillRect(3+plot_left+i*12,plot_top-h,10,h-1);
				g.setColor(Color.black);
				g.drawChars(new char[]{(char) ('A'+i)},0,1,4+plot_left+i*12, plot_top+12);
			}
		
	}

	boolean checkCount() {
		if ( count == null ) return false;
		int i;
		for ( i = 0 ; i < count.length ; ++i )
			if ( count[i] != 0 ) return true;
		return false;
	}

	public static void main(String[] args) {
		LetterHistogram h = new LetterHistogram();
	}

}
