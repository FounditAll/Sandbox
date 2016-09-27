/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
ABOUT: this is the main class in the Spectrogram analysis system. It handles the information processed by the Spectrogram analysis system.
*/


package Spectrogram;
//Import all necessary items
import Spectrogram.PitchDetectionPanel;
import Spectrogram.InputPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import be.tarsos.dsp.util.fft.FFT;

//create a public class called Spectogram that extends JFrame and implements PitchDectetionHandler
public class Spectrogram extends JFrame implements PitchDetectionHandler {
	
	//initalize all variables, make private
	private static final long serialVersionUID = 1383896180290138076L;
	private final SpectrogramPanel panel;
	private AudioDispatcher dispatcher;
	private Mixer currentMixer;	
	private PitchEstimationAlgorithm algo;
	private double pitch; 
	
	private float sampleRate = 44100;
	private int bufferSize = 1024 * 4;
	private int overlap = 768 * 4 ;
	
	private String fileName;
	
	//create new ActionListener, set equal to new ActionListener() taking no parameters
	private ActionListener algoChangeListener = new ActionListener(){
            //add @Override
		@Override
                // create method called actionPerformed that takes a final ActionEvent
		public void actionPerformed(final ActionEvent e) {
                    //create String name, access ActionEvent, set name equal to method .getActionCommand() 
			String name = e.getActionCommand();
                        //create new PitchEstimationAlgorithm set equal to the result of the PitchEstimaitonAlgorithm method .valueOf(String) taking name as the parameter 
			PitchEstimationAlgorithm newAlgo = PitchEstimationAlgorithm.valueOf(name);
                        //set algo to newAlgo
			algo = newAlgo;
                        //create try/ catch loop
			try {
                            //activate setNewMixer method, takes currentMixer as parameter
				setNewMixer(currentMixer);
                          //set catch for an unavailable line
			} catch (LineUnavailableException e1) {
                                // trace the problem
				e1.printStackTrace();
                          //set catch for an unsupported audio file
			} catch (UnsupportedAudioFileException e1) {
                                //trace the problem
				e1.printStackTrace();
			}
	}};
		// create public constructor
	public Spectrogram(String fileName){
            //this.----- refers to current method
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Spectrogram");
		panel = new SpectrogramPanel();
		algo = PitchEstimationAlgorithm.DYNAMIC_WAVELET;
		this.fileName = fileName;
		
                //create new JPanel set equal to new PitchDetectionPanel with parameters algoChangeListener
		JPanel pitchDetectionPanel = new PitchDetectionPanel(algoChangeListener);
		
                //create new JPanel set equal to new InputPanel()
		JPanel inputPanel = new InputPanel();
	
                //access addPropertyChangeListener() in inputPanel variable, set parameters to "mixer", and new PropertyChangeListener()
		inputPanel.addPropertyChangeListener("mixer",
				new PropertyChangeListener() {
					@Override
                                        //create new public void method propertyChange() with PropertyChangeEvent variable as parameter
					public void propertyChange(PropertyChangeEvent arg0) {
                                            //
						try {
							setNewMixer((Mixer) arg0.getNewValue());
						} catch (LineUnavailableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (UnsupportedAudioFileException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
		
		JPanel containerPanel = new JPanel(new GridLayout(1,0));
		containerPanel.add(inputPanel);
		containerPanel.add(pitchDetectionPanel);
		this.add(containerPanel,BorderLayout.NORTH);
		
		JPanel otherContainer = new JPanel(new BorderLayout());
		otherContainer.add(panel,BorderLayout.CENTER);
		otherContainer.setBorder(new TitledBorder("3. Utter a sound (whistling works best)"));
		
		
		this.add(otherContainer,BorderLayout.CENTER);
	}

    public Spectrogram() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	
	
	
	private void setNewMixer(Mixer mixer) throws LineUnavailableException, UnsupportedAudioFileException {

		if(dispatcher!= null){
			dispatcher.stop();
		}
		if(fileName == null){
			final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true,
					false);
			final DataLine.Info dataLineInfo = new DataLine.Info(
					TargetDataLine.class, format);
			TargetDataLine line;
			line = (TargetDataLine) mixer.getLine(dataLineInfo);
			final int numberOfSamples = bufferSize;
			line.open(format, numberOfSamples);
			line.start();
			final AudioInputStream stream = new AudioInputStream(line);
                       
			JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
                         System.out.println(audioStream);
			// create a new dispatcher
			dispatcher = new AudioDispatcher(audioStream, bufferSize,
					overlap);
		} else {
			try {
				File audioFile = new File(fileName);
				dispatcher = AudioDispatcherFactory.fromFile(audioFile, bufferSize, overlap);
				AudioFormat format = AudioSystem.getAudioFileFormat(audioFile).getFormat();
				dispatcher.addAudioProcessor(new AudioPlayer(format));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		currentMixer = mixer;

		// add a processor, handle pitch event.
		dispatcher.addAudioProcessor(new PitchProcessor(algo, sampleRate, bufferSize, this));
		dispatcher.addAudioProcessor(fftProcessor);

		// run the dispatcher (on a new thread).
		new Thread(dispatcher,"Audio dispatching").start();
	}
	
	AudioProcessor fftProcessor = new AudioProcessor(){
		
		FFT fft = new FFT(bufferSize);
		float[] amplitudes = new float[bufferSize/2];

		@Override
		public void processingFinished() {
			// TODO Auto-generated method stub
		}

		@Override
                
                //extract info from here?
		public boolean process(AudioEvent audioEvent) {
                        
                        
			float[] audioFloatBuffer = audioEvent.getFloatBuffer();
			float[] transformbuffer = new float[bufferSize*2];
			System.arraycopy(audioFloatBuffer, 0, transformbuffer, 0, audioFloatBuffer.length); 
			fft.forwardTransform(transformbuffer);
			fft.modulus(transformbuffer, amplitudes);
                        float[] amplitudesTwo = modulus(transformbuffer, amplitudes);
                        // just extract current pitch from spectrogramPanel, amplitude 
                        System.out.println("Pitch: " + panel.currentPitch);
//                        System.out.println("Amplitudes: " + writeAmplitudes(amplitudes));
                        System.out.println("Amplitudes 2: " + writeAmplitudesTwo(amplitudesTwo));
                        System.out.println("Time: " + System.nanoTime());
                        System.out.println("FFT: " + fft);
			panel.drawFFT(pitch, amplitudes,fft);
			panel.repaint();
			return true;
		}
		
	};
	
	@Override
	public void handlePitch(PitchDetectionResult pitchDetectionResult,AudioEvent audioEvent) {
		if(pitchDetectionResult.isPitched()){
			pitch = pitchDetectionResult.getPitch();
		} else {
			pitch = -1;
		}
		
	}
        
        public String writeAmplitudes(float[] amplitudes){
            String results = "";
            for(int a = 0; a < amplitudes.length; a++){
                results = results + " " + Float.toString(amplitudes[a]);
            }
            return results;
        }
        
        public String writeAmplitudesTwo(float[] amplitudes){
             String results = "";
            for(int a = 0; a < amplitudes.length; a++){
                results = results + " " + Float.toString(amplitudes[a]);
            }
            return results;
        }
        
        public float[] modulus(final float[] data, final float[] amplitudes) {
                float[] newAmplitudes = new float[amplitudes.length];
		assert data.length / 2 == amplitudes.length;
                
		for (int i = 0; i < amplitudes.length; i++) {
                final int index = i;
                final int realIndex = 2 * index;
		final int imgIndex =  2 * index + 1;
		final float modulus = data[realIndex] * data[realIndex] + data[imgIndex] * data[imgIndex];
		double a = Math.sqrt(modulus);
                newAmplitudes[i] = 
                        (float) a;
			
		}
                return newAmplitudes;
	}
        
        //this code results in an incmpatible types error. "float[] cannot convert to int"
//        public float modulus(final float[] data, final int index) {
//		final int realIndex = 2 * index;
//		final int imgIndex =  2 * index + 1;
//		final float modulus = data[realIndex] * data[realIndex] + data[imgIndex] * data[imgIndex];
//		return (float) Math.sqrt(modulus);
//	}
	
	public static void main(final String... strings) throws InterruptedException,
                //PSV Main, program run begins here
			InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
                            
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					// ignore failure to set default look en feel;
				}
				JFrame frame = strings.length == 0 ? new Spectrogram(null) : new Spectrogram(strings[0]) ;
				frame.pack();
				frame.setSize(640, 480);
				frame.setVisible(true);
			}
		});
}
    
	

}