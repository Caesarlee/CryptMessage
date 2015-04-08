package com.bruce.diffhell;
import java.math.BigInteger;
import java.security.SecureRandom;

public class SelfDefineDH {

	
	// 产生大素数P和它的本原根g
	public static BigInteger[] SecretKeySwap() {
		try {
			
			BigInteger[] bg = new BigInteger[2];
			bg[0] = BigInteger.ONE;
			bg[1] = BigInteger.ONE;
			int bitLength = 200;
			SecureRandom rnd = new SecureRandom();
			// BigInteger y = BigInteger.probablePrime(bitLength, rnd);
			BigInteger P = BigInteger.ONE;
			BigInteger g = BigInteger.ZERO;

			BigInteger gg = BigInteger.ONE;
			for (int j = 0; j < 200000; j++) {
				BigInteger q = BigInteger.probablePrime(bitLength, rnd);
				// System.out.println("Q=" + q);

				BigInteger pp = BigInteger.valueOf(2).multiply(q)
						.add(BigInteger.valueOf(1));

				if (pp.isProbablePrime(100)) {
				//	Log.i("DEVIL","第" + j + "次找到" + "P=" + pp);
					P = pp;
					for (int i = 2; i < 5000; i++) {
						gg = new BigInteger(String.valueOf(i));
						if (gg.modPow(BigInteger.valueOf(2), pp).intValue() == 1
								|| gg.modPow(q, pp).intValue() == 1) {
							continue;
						} else {
							g = gg;
							bg[0] = P;
							bg[1] = g;
						//	Log.i("DEVIL","P=" + P.toString() + "\ng="
						//			+ g.toString());

							return bg;
							// System.out.println("P="+P.toString()+"\ng="+g.toString());

						}

					}
					if (g.equals(gg))
						break;

				} else {
					continue;
				}

			}

		} catch (Exception exc) {

		}
		return null;
	}

	// A方产生自己的Xa,Ya
	public static BigInteger[] generateXY_A(BigInteger[] bgArr) {
		BigInteger[] Axy = new BigInteger[2];
		// 选一个Xa且<素数P

		BigInteger Xa = BigInteger.ZERO;

		Xa = bgArr[0].divide(BigInteger.valueOf(100000000));
		Axy[0] = Xa;
		BigInteger Ya = BigInteger.ONE;

		Ya = bgArr[1].modPow(Xa, bgArr[0]);
		Axy[1] = Ya;
		
		//System.out.println("Xa="+Xa.toString());
		//System.out.println("Ya="+Ya.toString());
		return Axy;
	}

	// B方产生自己的Xb,Yb
	public static BigInteger[] generateXY_B(BigInteger[] bgArr) {
		BigInteger[] Bxy = new BigInteger[2];
		// 选一个Xb且<素数P

		BigInteger Xb = BigInteger.ZERO;

		Xb = bgArr[0].divide(BigInteger.valueOf(1002400000));
		Bxy[0] = Xb;
		BigInteger Yb = BigInteger.ONE;

		Yb = bgArr[1].modPow(Xb, bgArr[0]);
		Bxy[1] = Yb;

		//System.out.println("Xb="+Xb.toString());
		//System.out.println("Yb="+Yb.toString());
		
		return Bxy;
	}
}
