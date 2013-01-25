package org.nlogo.plot

import org.nlogo.mirror.Action

sealed trait PlotAction extends Action

object PlotAction {

  case class ClearPlot(plotName: String)
    extends PlotAction

  case class PlotY(plotName: String, penName: String, y: Double)
    extends PlotAction

  case class PlotXY(plotName: String, penName: String, x: Double, y: Double)
    extends PlotAction

  case class Histogram(plotName: String, penName: String, values: Seq[Double])
    extends PlotAction

  case class AutoPlot(plotName: String, on: Boolean)
    extends PlotAction

  case class SetRange(plotName: String, isX: Boolean, min: Double, max: Double)
    extends PlotAction

  case class PenDown(plotName: String, penName: String, down: Boolean)
    extends PlotAction

  case class HidePen(plotName: String, penName: String, hidden: Boolean)
    extends PlotAction

  case class ResetPen(plotName: String, penName: String)
    extends PlotAction

  case class SetPenInterval(plotName: String, penName: String, interval: Double)
    extends PlotAction

  case class SetPenMode(plotName: String, penName: String, mode: Int)
    extends PlotAction

  case class SetPenColor(plotName: String, penName: String, color: Int)
    extends PlotAction

  case class CreateTemporaryPen(plotName: String, penName: String)
    extends PlotAction

}
