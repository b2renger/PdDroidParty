package cx.mccormick.pddroidparty.midi;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.puredata.core.PdBase;

import de.humatic.nmj.NetworkMidiClient;
import de.humatic.nmj.NetworkMidiOutput;

public class MidiClock implements NetworkMidiClient{

	private volatile boolean shouldSendClock = false;
	private volatile int bpm;
	private Semaphore semaphore = new Semaphore(0);
	
	public void start(final NetworkMidiOutput out, int startBpm)
	{
		if(!shouldSendClock)
		{
			this.bpm = startBpm;
			shouldSendClock = true;
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					
					try {
							
							System.out.println("send MIDI start");
							//out.sendMidi(new byte[]{(byte)0xFC});
							if(out != null) out.sendMidi(new byte[]{(byte)0xFA});
							PdBase.sendSysRealTime(0, 0xFA);
							
							while(shouldSendClock)
							{
								// TODO System.nanoTime() to get perfect clock
								
								if(out != null) out.sendMidi(new byte[]{(byte)0xF8});
								
								PdBase.sendSysRealTime(0, 0xF8);
								
								TimeUnit.MICROSECONDS.sleep(60000000 / (bpm * 24));
							}
							
							System.out.println("send MIDI stop");
							if(out != null) out.sendMidi(new byte[]{(byte)0xFC});
							PdBase.sendSysRealTime(0, 0xFC);

							semaphore.release();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}).start();
		}
	}
	
	public void stop()
	{
		if(shouldSendClock)
		{
			shouldSendClock = false;
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void setBPM(int value)
	{
		bpm = value;
	}
	
	

}
