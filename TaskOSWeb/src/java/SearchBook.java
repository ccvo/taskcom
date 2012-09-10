
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Chuong Vo
 */
public final class SearchBook extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String book = request.getParameter("book");

        try {
            if (book != null) {
                if (book.equalsIgnoreCase("java")) {
                    out.print("LOCATION: Bundoora General Section, Level 3. CALL NO: 005.133 S3342. STATUS: Available.");
                } else if (book.equalsIgnoreCase("database")) {
                    out.print("LOCATION: Bundoora Reserve Collection Section, Level 1. CALL NO: 005.74 E482 c.2. STATUS: Available.");
                } else if (book.equalsIgnoreCase("programming")) {
                    out.print("LOCATION: Bundoora 7 DAY LOAN Section, Level 2. CALL NO: 005.133 M2512. STATUS: Available.");
                } else {
                    out.print("Couldn't find the book '" + book + "'!");
                }
            }else{
                out.print("Please provide the name of the book!");
            }
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
