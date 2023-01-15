package model.data.parse.site.wrapper

import org.jsoup.nodes.Element
import controller.data.getter.SiteParser


/**
 * Wrapper class to iterator variables, that used by [site parser function][SiteParser.getAvailableNodes].
 * May be useful on a debug process (because you can check values directly).
 *
 * @param generalChangesElement  Container that contains all month-scoped targetChangesOfDay elements.
 *                               It gets by CSS-Selector.
 * @param listOfMonthChangesElements  List of containers, that contains day targetChangesOfDay elements.
 *                                    It gets from parent node, by '.children()' function.
 */
class ChangeElementsWrapper(val generalChangesElement: Element?,
                            val listOfMonthChangesElements: List<Element>?)
