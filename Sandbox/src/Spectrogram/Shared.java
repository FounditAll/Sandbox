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
* this class is part of the Spectrogram analysis system. it handles getting the mixer information, retrieving local string, and retrieving OS name.
*/


package Spectrogram;

import java.util.Vector;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
//

//create public class; name Shared
public class Shared {
	//create public static Vector which takes two parameters (final boolean supportsPlayback, and final boolean supportsRecording; name gatMixerInfo
        //setting variables as final means that after they are assigned, any attempt to change them will result in a compiler error
	public static Vector<Mixer.Info> getMixerInfo(final boolean supportsPlayback, final boolean supportsRecording) {
                //
		final Vector<Mixer.Info> infos = new Vector<Mixer.Info>();
                //
		final Mixer.Info[] mixers = AudioSystem.getMixerInfo();
                //
		for (final Info mixerinfo : mixers) {
			if (supportsRecording
					&& AudioSystem.getMixer(mixerinfo).getTargetLineInfo().length != 0) {
				// Mixer capable of recording audio if target LineWavelet length != 0
				infos.add(mixerinfo);
			} else if (supportsPlayback
					&& AudioSystem.getMixer(mixerinfo).getSourceLineInfo().length != 0) {
				// Mixer capable of audio play back if source LineWavelet length != 0
				infos.add(mixerinfo);
			}
		}
		return infos;
	}
        //create public method that returns a string and takes and object (info) as its parameter; name toLocalString
	public static String toLocalString(Object info)
	{
            //checks if system is windows
            // if is not windows
		if(!isWindows())
                        // call toString on info object
			return info.toString();
                //set String defaultEncoding equal to the the toString method of the defaultCharset method
		String defaultEncoding = Charset.defaultCharset().toString();
		try
		{
                        //get getBytes method of object info's toString method for a string
                        //combine with defaultEncoding
                        //bind to new String
                        //return to querying party
			return new String(info.toString().getBytes("windows-1252"), defaultEncoding);
		}
                //catch new error "unsupportedEncodingException"
		catch(UnsupportedEncodingException ex)
		{
                        //return toString method of object info to querying party
			return info.toString();
		}
	}
        //create private variable, set to null; name OS
	private static String OS = null;
        //create new method that returns a string; name getOsName
	public static String getOsName()
	{
            //checks if system is Mac OS
            //if the name is not available
		if(OS == null)
                         //use System.getProperty to retrieve name
                         //bind name to string variable OS
			OS = System.getProperty("os.name");
            //return the name of OS to querying party           
	    return OS;
            
	}
        //create public static boolean; name isWindows
        //check for windows
        //if windows
	public static boolean isWindows()
	{
           // get name of OS
           // return it to querying party 
	   return getOsName().startsWith("Windows");
	}
}
