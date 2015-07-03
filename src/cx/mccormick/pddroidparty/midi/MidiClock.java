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
		start(out, startBpm, true);
	}
	private void start(final NetworkMidiOutput out, int startBpm, final boolean sendStartMessage)
	{
		if(!shouldSendClock)
		{
			this.bpm = startBpm;
			shouldSendClock = true;
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					
					try {

							if(sendStartMessage)
							{
								System.out.println("send MIDI start");
								if(out != null) out.sendMidi(new byte[]{(byte)MidiCode.MIDI_REALTIME_CLOCK_START});
								PdBase.sendSysRealTime(0, MidiCode.MIDI_REALTIME_CLOCK_START);
							}
							else
							{
								System.out.println("send MIDI resume");
								if(out != null) out.sendMidi(new byte[]{(byte)MidiCode.MIDI_REALTIME_CLOCK_RESUME});
								PdBase.sendSysRealTime(0, MidiCode.MIDI_REALTIME_CLOCK_RESUME);
							}
							
							while(shouldSendClock)
							{
								// TODO System.nanoTime() to get perfect clock
								
								if(out != null) out.sendMidi(new byte[]{(byte)MidiCode.MIDI_REALTIME_CLOCK_TICK});
								
								PdBase.sendSysRealTime(0, MidiCode.MIDI_REALTIME_CLOCK_TICK);
								
								TimeUnit.MICROSECONDS.sleep(60000000 / (bpm * 24));
							}
							
							System.out.println("send MIDI stop");
							if(out != null) out.sendMidi(new byte[]{(byte)MidiCode.MIDI_REALTIME_CLOCK_STOP});
							PdBase.sendSysRealTime(0, MidiCode.MIDI_REALTIME_CLOCK_STOP);

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

	public void resume(final NetworkMidiOutput out, int startBpm) 
	{
		start(out, startBpm, false);
	}
	
	

}
