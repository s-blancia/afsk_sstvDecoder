/* code generated by 'utils/stft.c' */
static const int stft_N = 1024;
static const float stft_w[1024] = {
	1.45188e-08,
	1.52445e-08,
	1.60051e-08,
	1.6802e-08,
	1.76369e-08,
	1.85115e-08,
	1.94276e-08,
	2.03871e-08,
	2.13919e-08,
	2.24442e-08,
	2.3546e-08,
	2.46994e-08,
	2.5907e-08,
	2.71709e-08,
	2.84939e-08,
	2.98784e-08,
	3.13271e-08,
	3.2843e-08,
	3.4429e-08,
	3.6088e-08,
	3.78235e-08,
	3.96386e-08,
	4.15369e-08,
	4.35219e-08,
	4.55974e-08,
	4.77673e-08,
	5.00358e-08,
	5.24069e-08,
	5.48852e-08,
	5.74753e-08,
	6.01818e-08,
	6.30097e-08,
	6.59643e-08,
	6.90507e-08,
	7.22748e-08,
	7.56421e-08,
	7.91588e-08,
	8.2831e-08,
	8.66654e-08,
	9.06686e-08,
	9.48477e-08,
	9.92099e-08,
	1.03763e-07,
	1.08514e-07,
	1.13473e-07,
	1.18646e-07,
	1.24044e-07,
	1.29675e-07,
	1.35548e-07,
	1.41674e-07,
	1.48063e-07,
	1.54725e-07,
	1.61672e-07,
	1.68914e-07,
	1.76464e-07,
	1.84333e-07,
	1.92535e-07,
	2.01083e-07,
	2.09991e-07,
	2.19272e-07,
	2.28942e-07,
	2.39015e-07,
	2.49507e-07,
	2.60436e-07,
	2.71817e-07,
	2.83668e-07,
	2.96008e-07,
	3.08855e-07,
	3.2223e-07,
	3.36151e-07,
	3.5064e-07,
	3.65719e-07,
	3.8141e-07,
	3.97736e-07,
	4.14721e-07,
	4.32391e-07,
	4.5077e-07,
	4.69886e-07,
	4.89766e-07,
	5.10438e-07,
	5.31932e-07,
	5.54278e-07,
	5.77508e-07,
	6.01654e-07,
	6.2675e-07,
	6.5283e-07,
	6.79931e-07,
	7.08089e-07,
	7.37343e-07,
	7.67733e-07,
	7.99298e-07,
	8.32083e-07,
	8.66129e-07,
	9.01482e-07,
	9.38189e-07,
	9.76298e-07,
	1.01586e-06,
	1.05692e-06,
	1.09954e-06,
	1.14376e-06,
	1.18965e-06,
	1.23727e-06,
	1.28666e-06,
	1.33791e-06,
	1.39106e-06,
	1.44618e-06,
	1.50335e-06,
	1.56262e-06,
	1.62408e-06,
	1.68779e-06,
	1.75384e-06,
	1.8223e-06,
	1.89324e-06,
	1.96677e-06,
	2.04295e-06,
	2.12188e-06,
	2.20365e-06,
	2.28836e-06,
	2.37609e-06,
	2.46695e-06,
	2.56104e-06,
	2.65847e-06,
	2.75934e-06,
	2.86377e-06,
	2.97186e-06,
	3.08374e-06,
	3.19953e-06,
	3.31934e-06,
	3.44332e-06,
	3.57158e-06,
	3.70427e-06,
	3.84153e-06,
	3.98349e-06,
	4.1303e-06,
	4.28211e-06,
	4.43908e-06,
	4.60136e-06,
	4.76913e-06,
	4.94254e-06,
	5.12176e-06,
	5.30698e-06,
	5.49837e-06,
	5.69612e-06,
	5.90042e-06,
	6.11146e-06,
	6.32945e-06,
	6.55459e-06,
	6.78709e-06,
	7.02717e-06,
	7.27505e-06,
	7.53095e-06,
	7.79511e-06,
	8.06776e-06,
	8.34916e-06,
	8.63955e-06,
	8.93918e-06,
	9.24833e-06,
	9.56725e-06,
	9.89622e-06,
	1.02355e-05,
	1.05855e-05,
	1.09463e-05,
	1.13184e-05,
	1.1702e-05,
	1.20975e-05,
	1.25051e-05,
	1.29252e-05,
	1.33582e-05,
	1.38044e-05,
	1.42641e-05,
	1.47377e-05,
	1.52256e-05,
	1.57281e-05,
	1.62457e-05,
	1.67787e-05,
	1.73275e-05,
	1.78926e-05,
	1.84743e-05,
	1.90731e-05,
	1.96895e-05,
	2.03239e-05,
	2.09767e-05,
	2.16483e-05,
	2.23394e-05,
	2.30504e-05,
	2.37816e-05,
	2.45338e-05,
	2.53073e-05,
	2.61027e-05,
	2.69206e-05,
	2.77614e-05,
	2.86258e-05,
	2.95143e-05,
	3.04274e-05,
	3.13658e-05,
	3.233e-05,
	3.33208e-05,
	3.43386e-05,
	3.53841e-05,
	3.6458e-05,
	3.75608e-05,
	3.86934e-05,
	3.98563e-05,
	4.10503e-05,
	4.22759e-05,
	4.3534e-05,
	4.48253e-05,
	4.61505e-05,
	4.75104e-05,
	4.89056e-05,
	5.0337e-05,
	5.18054e-05,
	5.33115e-05,
	5.48561e-05,
	5.64402e-05,
	5.80644e-05,
	5.97297e-05,
	6.14369e-05,
	6.31869e-05,
	6.49805e-05,
	6.68186e-05,
	6.87022e-05,
	7.06321e-05,
	7.26094e-05,
	7.46349e-05,
	7.67095e-05,
	7.88343e-05,
	8.10103e-05,
	8.32383e-05,
	8.55195e-05,
	8.78549e-05,
	9.02453e-05,
	9.2692e-05,
	9.5196e-05,
	9.77582e-05,
	0.00010038,
	0.000103062,
	0.000105806,
	0.000108612,
	0.000111482,
	0.000114417,
	0.000117419,
	0.000120487,
	0.000123624,
	0.00012683,
	0.000130108,
	0.000133457,
	0.000136879,
	0.000140376,
	0.000143948,
	0.000147597,
	0.000151324,
	0.00015513,
	0.000159018,
	0.000162986,
	0.000167038,
	0.000171175,
	0.000175397,
	0.000179706,
	0.000184104,
	0.000188591,
	0.000193169,
	0.000197839,
	0.000202603,
	0.000207462,
	0.000212417,
	0.000217469,
	0.000222621,
	0.000227873,
	0.000233227,
	0.000238683,
	0.000244244,
	0.000249911,
	0.000255685,
	0.000261567,
	0.000267559,
	0.000273662,
	0.000279878,
	0.000286207,
	0.000292652,
	0.000299214,
	0.000305893,
	0.000312692,
	0.000319611,
	0.000326652,
	0.000333817,
	0.000341106,
	0.000348521,
	0.000356063,
	0.000363734,
	0.000371534,
	0.000379466,
	0.00038753,
	0.000395728,
	0.000404061,
	0.000412529,
	0.000421135,
	0.00042988,
	0.000438764,
	0.00044779,
	0.000456957,
	0.000466267,
	0.000475722,
	0.000485322,
	0.000495069,
	0.000504964,
	0.000515007,
	0.000525199,
	0.000535543,
	0.000546037,
	0.000556685,
	0.000567486,
	0.000578441,
	0.000589552,
	0.000600819,
	0.000612243,
	0.000623824,
	0.000635564,
	0.000647463,
	0.000659522,
	0.000671741,
	0.000684122,
	0.000696664,
	0.000709369,
	0.000722236,
	0.000735267,
	0.000748462,
	0.00076182,
	0.000775343,
	0.000789031,
	0.000802884,
	0.000816902,
	0.000831086,
	0.000845436,
	0.000859951,
	0.000874632,
	0.000889478,
	0.000904491,
	0.000919669,
	0.000935013,
	0.000950522,
	0.000966196,
	0.000982035,
	0.000998038,
	0.00101421,
	0.00103054,
	0.00104703,
	0.00106369,
	0.00108051,
	0.00109749,
	0.00111463,
	0.00113193,
	0.00114939,
	0.001167,
	0.00118478,
	0.00120271,
	0.00122079,
	0.00123903,
	0.00125742,
	0.00127596,
	0.00129466,
	0.0013135,
	0.00133249,
	0.00135162,
	0.0013709,
	0.00139032,
	0.00140988,
	0.00142958,
	0.00144942,
	0.00146939,
	0.00148949,
	0.00150973,
	0.00153009,
	0.00155059,
	0.0015712,
	0.00159194,
	0.0016128,
	0.00163378,
	0.00165487,
	0.00167607,
	0.00169739,
	0.00171881,
	0.00174033,
	0.00176196,
	0.00178368,
	0.0018055,
	0.00182742,
	0.00184942,
	0.00187151,
	0.00189368,
	0.00191594,
	0.00193827,
	0.00196067,
	0.00198314,
	0.00200568,
	0.00202828,
	0.00205094,
	0.00207366,
	0.00209642,
	0.00211924,
	0.0021421,
	0.002165,
	0.00218793,
	0.0022109,
	0.0022339,
	0.00225692,
	0.00227996,
	0.00230301,
	0.00232608,
	0.00234915,
	0.00237223,
	0.0023953,
	0.00241837,
	0.00244143,
	0.00246447,
	0.00248749,
	0.00251049,
	0.00253346,
	0.00255639,
	0.00257929,
	0.00260214,
	0.00262495,
	0.0026477,
	0.0026704,
	0.00269303,
	0.0027156,
	0.00273809,
	0.00276051,
	0.00278285,
	0.0028051,
	0.00282725,
	0.00284931,
	0.00287127,
	0.00289312,
	0.00291486,
	0.00293649,
	0.00295799,
	0.00297936,
	0.00300061,
	0.00302171,
	0.00304268,
	0.0030635,
	0.00308416,
	0.00310467,
	0.00312502,
	0.0031452,
	0.00316521,
	0.00318505,
	0.0032047,
	0.00322416,
	0.00324344,
	0.00326252,
	0.0032814,
	0.00330007,
	0.00331853,
	0.00333678,
	0.00335481,
	0.00337261,
	0.00339019,
	0.00340753,
	0.00342463,
	0.00344149,
	0.00345811,
	0.00347447,
	0.00349058,
	0.00350643,
	0.00352201,
	0.00353733,
	0.00355237,
	0.00356714,
	0.00358162,
	0.00359583,
	0.00360974,
	0.00362337,
	0.0036367,
	0.00364972,
	0.00366245,
	0.00367487,
	0.00368698,
	0.00369878,
	0.00371026,
	0.00372142,
	0.00373226,
	0.00374278,
	0.00375297,
	0.00376282,
	0.00377234,
	0.00378153,
	0.00379037,
	0.00379888,
	0.00380704,
	0.00381485,
	0.00382232,
	0.00382943,
	0.0038362,
	0.0038426,
	0.00384865,
	0.00385435,
	0.00385968,
	0.00386465,
	0.00386926,
	0.00387351,
	0.00387739,
	0.0038809,
	0.00388405,
	0.00388683,
	0.00388924,
	0.00389128,
	0.00389295,
	0.00389425,
	0.00389518,
	0.00389574,
	0.00389592,
	0.00389574,
	0.00389518,
	0.00389425,
	0.00389295,
	0.00389128,
	0.00388924,
	0.00388683,
	0.00388405,
	0.0038809,
	0.00387739,
	0.00387351,
	0.00386926,
	0.00386465,
	0.00385968,
	0.00385435,
	0.00384865,
	0.0038426,
	0.0038362,
	0.00382943,
	0.00382232,
	0.00381485,
	0.00380704,
	0.00379888,
	0.00379037,
	0.00378153,
	0.00377234,
	0.00376282,
	0.00375297,
	0.00374278,
	0.00373226,
	0.00372142,
	0.00371026,
	0.00369878,
	0.00368698,
	0.00367487,
	0.00366245,
	0.00364972,
	0.0036367,
	0.00362337,
	0.00360974,
	0.00359583,
	0.00358162,
	0.00356714,
	0.00355237,
	0.00353733,
	0.00352201,
	0.00350643,
	0.00349058,
	0.00347447,
	0.00345811,
	0.00344149,
	0.00342463,
	0.00340753,
	0.00339019,
	0.00337261,
	0.00335481,
	0.00333678,
	0.00331853,
	0.00330007,
	0.0032814,
	0.00326252,
	0.00324344,
	0.00322416,
	0.0032047,
	0.00318505,
	0.00316521,
	0.0031452,
	0.00312502,
	0.00310467,
	0.00308416,
	0.0030635,
	0.00304268,
	0.00302171,
	0.00300061,
	0.00297936,
	0.00295799,
	0.00293649,
	0.00291486,
	0.00289312,
	0.00287127,
	0.00284931,
	0.00282725,
	0.0028051,
	0.00278285,
	0.00276051,
	0.00273809,
	0.0027156,
	0.00269303,
	0.0026704,
	0.0026477,
	0.00262495,
	0.00260214,
	0.00257929,
	0.00255639,
	0.00253346,
	0.00251049,
	0.00248749,
	0.00246447,
	0.00244143,
	0.00241837,
	0.0023953,
	0.00237223,
	0.00234915,
	0.00232608,
	0.00230301,
	0.00227996,
	0.00225692,
	0.0022339,
	0.0022109,
	0.00218793,
	0.002165,
	0.0021421,
	0.00211924,
	0.00209642,
	0.00207366,
	0.00205094,
	0.00202828,
	0.00200568,
	0.00198314,
	0.00196067,
	0.00193827,
	0.00191594,
	0.00189368,
	0.00187151,
	0.00184942,
	0.00182742,
	0.0018055,
	0.00178368,
	0.00176196,
	0.00174033,
	0.00171881,
	0.00169739,
	0.00167607,
	0.00165487,
	0.00163378,
	0.0016128,
	0.00159194,
	0.0015712,
	0.00155059,
	0.00153009,
	0.00150973,
	0.00148949,
	0.00146939,
	0.00144942,
	0.00142958,
	0.00140988,
	0.00139032,
	0.0013709,
	0.00135162,
	0.00133249,
	0.0013135,
	0.00129466,
	0.00127596,
	0.00125742,
	0.00123903,
	0.00122079,
	0.00120271,
	0.00118478,
	0.001167,
	0.00114939,
	0.00113193,
	0.00111463,
	0.00109749,
	0.00108051,
	0.00106369,
	0.00104703,
	0.00103054,
	0.00101421,
	0.000998038,
	0.000982035,
	0.000966196,
	0.000950522,
	0.000935013,
	0.000919669,
	0.000904491,
	0.000889478,
	0.000874632,
	0.000859951,
	0.000845436,
	0.000831086,
	0.000816902,
	0.000802884,
	0.000789031,
	0.000775343,
	0.00076182,
	0.000748462,
	0.000735267,
	0.000722236,
	0.000709369,
	0.000696664,
	0.000684122,
	0.000671741,
	0.000659522,
	0.000647463,
	0.000635564,
	0.000623824,
	0.000612243,
	0.000600819,
	0.000589552,
	0.000578441,
	0.000567486,
	0.000556685,
	0.000546037,
	0.000535543,
	0.000525199,
	0.000515007,
	0.000504964,
	0.000495069,
	0.000485322,
	0.000475722,
	0.000466267,
	0.000456957,
	0.00044779,
	0.000438764,
	0.00042988,
	0.000421135,
	0.000412529,
	0.000404061,
	0.000395728,
	0.00038753,
	0.000379466,
	0.000371534,
	0.000363734,
	0.000356063,
	0.000348521,
	0.000341106,
	0.000333817,
	0.000326652,
	0.000319611,
	0.000312692,
	0.000305893,
	0.000299214,
	0.000292652,
	0.000286207,
	0.000279878,
	0.000273662,
	0.000267559,
	0.000261567,
	0.000255685,
	0.000249911,
	0.000244244,
	0.000238683,
	0.000233227,
	0.000227873,
	0.000222621,
	0.000217469,
	0.000212417,
	0.000207462,
	0.000202603,
	0.000197839,
	0.000193169,
	0.000188591,
	0.000184104,
	0.000179706,
	0.000175397,
	0.000171175,
	0.000167038,
	0.000162986,
	0.000159018,
	0.00015513,
	0.000151324,
	0.000147597,
	0.000143948,
	0.000140376,
	0.000136879,
	0.000133457,
	0.000130108,
	0.00012683,
	0.000123624,
	0.000120487,
	0.000117419,
	0.000114417,
	0.000111482,
	0.000108612,
	0.000105806,
	0.000103062,
	0.00010038,
	9.77582e-05,
	9.5196e-05,
	9.2692e-05,
	9.02453e-05,
	8.78549e-05,
	8.55195e-05,
	8.32383e-05,
	8.10103e-05,
	7.88343e-05,
	7.67095e-05,
	7.46349e-05,
	7.26094e-05,
	7.06321e-05,
	6.87022e-05,
	6.68186e-05,
	6.49805e-05,
	6.31869e-05,
	6.14369e-05,
	5.97297e-05,
	5.80644e-05,
	5.64402e-05,
	5.48561e-05,
	5.33115e-05,
	5.18054e-05,
	5.0337e-05,
	4.89056e-05,
	4.75104e-05,
	4.61505e-05,
	4.48253e-05,
	4.3534e-05,
	4.22759e-05,
	4.10503e-05,
	3.98563e-05,
	3.86934e-05,
	3.75608e-05,
	3.6458e-05,
	3.53841e-05,
	3.43386e-05,
	3.33208e-05,
	3.233e-05,
	3.13658e-05,
	3.04274e-05,
	2.95143e-05,
	2.86258e-05,
	2.77614e-05,
	2.69206e-05,
	2.61027e-05,
	2.53073e-05,
	2.45338e-05,
	2.37816e-05,
	2.30504e-05,
	2.23394e-05,
	2.16483e-05,
	2.09767e-05,
	2.03239e-05,
	1.96895e-05,
	1.90731e-05,
	1.84743e-05,
	1.78926e-05,
	1.73275e-05,
	1.67787e-05,
	1.62457e-05,
	1.57281e-05,
	1.52256e-05,
	1.47377e-05,
	1.42641e-05,
	1.38044e-05,
	1.33582e-05,
	1.29252e-05,
	1.25051e-05,
	1.20975e-05,
	1.1702e-05,
	1.13184e-05,
	1.09463e-05,
	1.05855e-05,
	1.02355e-05,
	9.89622e-06,
	9.56725e-06,
	9.24833e-06,
	8.93918e-06,
	8.63955e-06,
	8.34916e-06,
	8.06776e-06,
	7.79511e-06,
	7.53095e-06,
	7.27505e-06,
	7.02717e-06,
	6.78709e-06,
	6.55459e-06,
	6.32945e-06,
	6.11146e-06,
	5.90042e-06,
	5.69612e-06,
	5.49837e-06,
	5.30698e-06,
	5.12176e-06,
	4.94254e-06,
	4.76913e-06,
	4.60136e-06,
	4.43908e-06,
	4.28211e-06,
	4.1303e-06,
	3.98349e-06,
	3.84153e-06,
	3.70427e-06,
	3.57158e-06,
	3.44332e-06,
	3.31934e-06,
	3.19953e-06,
	3.08374e-06,
	2.97186e-06,
	2.86377e-06,
	2.75934e-06,
	2.65847e-06,
	2.56104e-06,
	2.46695e-06,
	2.37609e-06,
	2.28836e-06,
	2.20365e-06,
	2.12188e-06,
	2.04295e-06,
	1.96677e-06,
	1.89324e-06,
	1.8223e-06,
	1.75384e-06,
	1.68779e-06,
	1.62408e-06,
	1.56262e-06,
	1.50335e-06,
	1.44618e-06,
	1.39106e-06,
	1.33791e-06,
	1.28666e-06,
	1.23727e-06,
	1.18965e-06,
	1.14376e-06,
	1.09954e-06,
	1.05692e-06,
	1.01586e-06,
	9.76298e-07,
	9.38189e-07,
	9.01482e-07,
	8.66129e-07,
	8.32083e-07,
	7.99298e-07,
	7.67733e-07,
	7.37343e-07,
	7.08089e-07,
	6.79931e-07,
	6.5283e-07,
	6.2675e-07,
	6.01654e-07,
	5.77508e-07,
	5.54278e-07,
	5.31932e-07,
	5.10438e-07,
	4.89766e-07,
	4.69886e-07,
	4.5077e-07,
	4.32391e-07,
	4.14721e-07,
	3.97736e-07,
	3.8141e-07,
	3.65719e-07,
	3.5064e-07,
	3.36151e-07,
	3.2223e-07,
	3.08855e-07,
	2.96008e-07,
	2.83668e-07,
	2.71817e-07,
	2.60436e-07,
	2.49507e-07,
	2.39015e-07,
	2.28942e-07,
	2.19272e-07,
	2.09991e-07,
	2.01083e-07,
	1.92535e-07,
	1.84333e-07,
	1.76464e-07,
	1.68914e-07,
	1.61672e-07,
	1.54725e-07,
	1.48063e-07,
	1.41674e-07,
	1.35548e-07,
	1.29675e-07,
	1.24044e-07,
	1.18646e-07,
	1.13473e-07,
	1.08514e-07,
	1.03763e-07,
	9.92099e-08,
	9.48477e-08,
	9.06686e-08,
	8.66654e-08,
	8.2831e-08,
	7.91588e-08,
	7.56421e-08,
	7.22748e-08,
	6.90507e-08,
	6.59643e-08,
	6.30097e-08,
	6.01818e-08,
	5.74753e-08,
	5.48852e-08,
	5.24069e-08,
	5.00358e-08,
	4.77673e-08,
	4.55974e-08,
	4.35219e-08,
	4.15369e-08,
	3.96386e-08,
	3.78235e-08,
	3.6088e-08,
	3.4429e-08,
	3.2843e-08,
	3.13271e-08,
	2.98784e-08,
	2.84939e-08,
	2.71709e-08,
	2.5907e-08,
	2.46994e-08,
	2.3546e-08,
	2.24442e-08,
	2.13919e-08,
	2.03871e-08,
	1.94276e-08,
	1.85115e-08,
	1.76369e-08,
	1.6802e-08,
	1.60051e-08,
	1.52445e-08
};
