package eu.kanade.tachiyomi.data.download

class ChapterData : Iterable<PageData>, AutoCloseable {
	object CacheLimit {
		// TODO: how to set these from configuration??? Hard code to sensible limits for now.
		// 300Mb total limit, 50Mb per chapter limit
		@set:Synchronized
		var totalLimit: Int = 300 * 1024 * 1024

		@set:Synchronized
		var chapterLimit: Int = 50 * 1024 * 1024

		var totalUsage: Int = 0
	}

	private val _pages: MutableList<PageData> = ArrayList()
	private var _size: Int = 0

	@get:Synchronized
	@set:Synchronized
	var notFull = true
		private set

	fun isFull(): Boolean {
		return !notFull
	}

	fun add(page: PageData): Boolean {
		var localNotFull = true

		synchronized(CacheLimit) {
			if ((_size + page.data.size < CacheLimit.chapterLimit) && (CacheLimit.totalUsage + page.data.size < CacheLimit.totalLimit)) {
				_pages.add(page)
				_size += page.data.size
				CacheLimit.totalUsage += page.data.size
			} else {
				localNotFull = false
			}
		}

		if (!localNotFull) {
			notFull = false
		}

		return localNotFull
	}

	override fun iterator(): Iterator<PageData> {
		return _pages.iterator()
	}

	override fun close() {
		synchronized(CacheLimit) {
			CacheLimit.totalUsage -= _size
		}
	}
}

class PageData(val name: String, val data: ByteArray)

