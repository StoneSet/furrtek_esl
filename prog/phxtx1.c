// Pricehax IR TX dongle firmware V1 for schematic V1
// furrtek 2014 - furrtek.org
// Apache License 2.0

// Code data buffering can be optimized a lot by doing bit-by-bit storage instead of byte-per-byte !

#define F_CPU 10000000

#include <avr/io.h>
#include <util/delay.h>
#include <inttypes.h>
#include <avr/interrupt.h>

volatile uint16_t codeptr = 0;
volatile uint8_t dcode[480];

#define NOP __asm__ __volatile__ ("nop")

ISR(TIMER1_OVF_vect) {
	uint8_t r,d;
	uint16_t pp;

	if (codeptr) {
		
		// PP4C playback magic !
		
		for (r=0;r<50;r++) {
			for (pp=0;pp<(codeptr-1);pp+=2) {
				TCCR0A = 0b01000010;
				TCCR0B = 0b00000001;
				_delay_us(39);			// 39 +1 -2 0 -2
				NOP;
				NOP;
				NOP;
				NOP;
				NOP;
				NOP;
				NOP;
				NOP;
				TCCR0A = 0b00000010;
				TCCR0B = 0b00000000;
				PORTB = 0;
				d = ((dcode[pp]<<1)+dcode[pp+1]);
				switch(d) {
				case 0:
					_delay_us(60-4);
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					break;
				case 1:
					_delay_us(60+60+60+60-3);
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					break;
				case 2:
					_delay_us(60+60-3);
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					NOP;
					break;
				case 3:
					_delay_us(60+60+60-2);
					break;
				}
			}
			TCCR0A = 0b01000010;
			TCCR0B = 0b00000001;
			_delay_us(39);			// 39 +1 -2 0 -2
			NOP;
			NOP;
			NOP;
			NOP;
			NOP;
			NOP;
			NOP;
			NOP;
			TCCR0A = 0b00000010;
			TCCR0B = 0b00000000;
			PORTB = 0;
			_delay_us(2000);
		}
	}
	codeptr = 0;
	ACSR = 0b01001010;
}

ISR(ANA_COMP_vect) {
	uint8_t tcval;

	tcval = TCNT1;
	TCNT1 = 0;

	if (codeptr) {
		if (tcval > 128)
			dcode[codeptr-1] = 0;
		else
			dcode[codeptr-1] = 1;
	}

	codeptr++;
}

int main(void) {
	WDTCR = (1<<WDCE) | (1<<WDE);
	WDTCR = 0x00;

	PORTB = 0;
	DDRB = 1;

	OCR0A = 3;	// 10/2/8=1.25MHz

	ACSR = 0b01001011;	//10
	DIDR0 = 0b00000010;

	TCCR1 = 0b00000101;
	TIMSK = 0b00000100;

	sei();

	for(;;) {}
}
