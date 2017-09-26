def Test():
	for k in range(4, 100, 2):
		for n in range(3, 30, 1):
			for d in range(2, 20, 1):
				if(k**(n-3) == 2**(n-3)*d):
					print(k,n,d, k**2/2)

if __name__ == "__main__":
	Test()