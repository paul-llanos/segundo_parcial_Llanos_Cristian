package controler;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.ItemCarrito;
import modelo.Producto;

public class ProductosController implements Initializable {

    @FXML
    private TableView<Producto> tablaProducto;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, Double> colPrecio;

    @FXML
    private TableColumn<Producto, Integer> colStock;

    @FXML
    private TextField txtCantidad;

    @FXML
    private ListView<ItemCarrito> listCarrito;

    @FXML
    private Label lblMensaje;

    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        cargarProductos();
        tablaProducto.setItems(listaProductos);
    }

    private void cargarProductos() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("productos.dat"))) {
            List<Producto> leidos = (List<Producto>) ois.readObject();
            listaProductos.setAll(leidos);
        } catch (IOException | ClassNotFoundException e) {
            if (lblMensaje != null) {
                lblMensaje.setText("No se pudo cargar productos.dat");
            }
        }
    }

    @FXML
    private void agregarCarrito() {
        Producto seleccionado = tablaProducto.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            if (lblMensaje != null) {
                lblMensaje.setText("Seleccioná un producto.");
            }
            return;
        }

        String textoCant = txtCantidad.getText().trim();
        if (textoCant.isEmpty()) {
            if (lblMensaje != null) {
                lblMensaje.setText("Ingresá una cantidad.");
            }
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(textoCant);
        } catch (NumberFormatException e) {
            if (lblMensaje != null) {
                lblMensaje.setText("Cantidad inválida.");
            }
            return;
        }

        if (cantidad <= 0 || cantidad > seleccionado.getStock()) {
            if (lblMensaje != null) {
                lblMensaje.setText("Stock insuficiente.");
            }
            return;
        }

        
        seleccionado.setStock(seleccionado.getStock() - cantidad);
        tablaProducto.refresh();

        listCarrito.getItems().add(new ItemCarrito(seleccionado, cantidad));
        txtCantidad.clear();
        if (lblMensaje != null) {
            lblMensaje.setText("Agregado al carrito.");
        }
    }

    @FXML
    private void confirmarCompra() {
        if (listCarrito.getItems().isEmpty()) {
            if (lblMensaje != null) {
                lblMensaje.setText("El carrito está vacío.");
            }
            return;
        }

        double total = 0.0;
        List<String> lineasTicket = new ArrayList<>();

        for (ItemCarrito item : listCarrito.getItems()) {
            double subtotal = item.getSubtotal();
            total += subtotal;
            lineasTicket.add(item.getProducto().getNombre() + " - " + item.getCantidad() + " - " + subtotal);
        }

        try {
            generarTicket(lineasTicket, total);
            guardarProductosDat();
            listCarrito.getItems().clear();
            tablaProducto.refresh();
            if (lblMensaje != null) {
                lblMensaje.setText("Compra confirmada. Ticket generado.");
            }
        } catch (IOException e) {
            if (lblMensaje != null) {
                lblMensaje.setText("Error al guardar ticket o productos.dat.");
            }
        }
    }

    private void generarTicket(List<String> lineas, double total) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("ticket.txt"))) {
            for (String linea : lineas) {
                bw.write(linea);
                bw.newLine();
            }
            bw.write("TOTAL A PAGAR: $" + total);
            bw.newLine();
        }
    }

    private void guardarProductosDat() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("productos.dat"))) {
            oos.writeObject(new ArrayList<>(listaProductos));
        }
    }
}