package com.lc.bxm.common.util;


public class SyncThread implements Runnable{
	       private static int count;
	 
	          public SyncThread() {
	              count = 0;
	         }
	          private  String dbname;
	
	         public SyncThread(String dbname ) {
	        	  this.dbname =dbname;
	          }

			public void run() {
	              synchronized(this) {
	            	  for (int i = 0; i < 5; i++) {
	                      try {
	                          System.err.println(dbname+ ":"+count++);
	                         Thread.sleep(1000);
	                     } catch (InterruptedException e) {
	                        e.printStackTrace();
	                    }
	                  }
	              }
	          }
}
