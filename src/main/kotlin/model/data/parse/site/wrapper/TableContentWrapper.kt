package model.data.parse.site.wrapper

import org.jsoup.nodes.Element
import org.jsoup.select.Elements


/**
 * This class wraps table content, that contain information with day changes.
 *
 * @param header  Represents <thead> element with table header.
 * @param body  Represents <tbody> an element with table body.
 * @param rows  Represents all table rows with target content.
 */
class TableContentWrapper(val header: Element, val body: Element,
                          val rows: Elements)
