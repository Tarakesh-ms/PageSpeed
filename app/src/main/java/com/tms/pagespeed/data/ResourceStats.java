/**
 * 
 */
package com.tms.pagespeed.data;

/**
 * @author Tarak
 *
 */
public final class ResourceStats {
	
	private final Long mFlashBytes;
	private final Long mJsBytes;
	private final Long mImageBytes;
	private final Long mCssBytes;
	private final Long mHtmlBytes;
	private final Long mTextBytes;
	private final Long mOtherBytes;
	
	/**
	 * @param flashBytes
	 * @param jsBytes
	 * @param imageBytes
	 * @param cssBytes
	 * @param htmlBytes
	 * @param otherBytes
	 */
	public ResourceStats(Long flashBytes, Long jsBytes, Long imageBytes, Long cssBytes, Long htmlBytes, 
			Long textBytes, Long otherBytes){
		mFlashBytes = flashBytes;
		mJsBytes 	= jsBytes;
		mImageBytes = imageBytes;
		mCssBytes	= cssBytes;
		mHtmlBytes  = htmlBytes;
		mTextBytes  = textBytes;
		mOtherBytes = otherBytes;
	}
	
	@Override
	public String toString() {
		String close = "]";
		String closeAppend = "],";

		StringBuilder resourcesBreakDown =
				new StringBuilder("['Resources', 'Breakdown'],")
					.append("['Flash',").append(mFlashBytes).append(closeAppend)
					.append("['Javascript',").append(mJsBytes).append(closeAppend)
					.append("['Images',").append(mImageBytes).append(closeAppend)
					.append("['CSS',").append(mCssBytes).append(closeAppend)
					.append("['HTML',").append(mHtmlBytes).append(closeAppend)
					.append("['Text',").append(mTextBytes).append(closeAppend)
					.append("['Other',").append(mOtherBytes).append(close);
				
		return resourcesBreakDown.toString();
	}

}
