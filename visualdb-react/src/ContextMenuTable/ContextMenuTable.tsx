import { FC } from "react";
import axios from "axios";

interface Table {
  id: string;
  nome: string;
  data_creazione: string;
  database: string;
  ora_creazione: string;
}

interface props {
  x: number;
  y: number;
  closeContextMenu: () => void;
  openModal: () => void;
  clickedTable: Table;
  setClickedTable: (clickedTable: Table) => void
  listOfTables: Table[];
  setListOfTables: (listOfTables: Table[]) => void
  setSelectedTable: (selectedTable: Table | null) => void
}

export const ContextMenuTable: FC<props> = ({x, y, closeContextMenu, openModal, clickedTable, setClickedTable, listOfTables, setListOfTables, setSelectedTable}) => {
  const deleteTable = async () => {
    try {
      const jwtoken = "Bearer $" + sessionStorage.getItem("jwtToken");
      console.debug(jwtoken);
      const data = await axios.delete(
        `http://localhost:8080/visualdb-api/table/delete?idDb=${clickedTable.database}&idTable=${clickedTable.id}&nameTable=${clickedTable.nome}`,
        {
          headers: {
            Authorization: jwtoken,
            "Content-Type": "application/json",
          },
        }
      );

      if (data.status === 200) {
        console.log(JSON.stringify(data, null, 4));
        const updatedTables = listOfTables.filter(table => table.id !== clickedTable.id);
        setListOfTables(updatedTables);
        setSelectedTable(null)
        setClickedTable({
          id: "0",
          nome: "nessun db selezionato",
          data_creazione: "0",
          database: "0",
          ora_creazione: "0",
        })
        closeContextMenu();
      } else {
        console.error(JSON.stringify(data, null, 4));
      }
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.log("error message: ", error.message);
        return error.message;
      } else {
        console.log("unexpected error: ", error);
        return "An unexpected error occurred";
      }
    }
  };

  return (
    <>
      <div
        onClick={() => closeContextMenu()}
        className="absolute z-40"
        style={{ top: `${y}px`, left: `${x}px` }}
      >
        <div
          id="dropdown"
          className="bg-white divide-y divide-gray-100 rounded-lg shadow w-44 dark:bg-gray-700"
        >
          <h1 className="px-6 py-2 font-medium text-white">
            {clickedTable.nome}
          </h1>
          <ul
            className="py-2 text-sm text-gray-700 dark:text-gray-200"
            aria-labelledby="dropdownDefaultButton"
          >
            <li>
              <button
                onClick={openModal}
                className="block px-4 py-2 w-full hover:bg-gray-100 dark:hover:bg-gray-600 dark:hover:text-white"
              >
                Rinomina
              </button>
            </li>
            <li>
              <button
                onClick={deleteTable}
                className="block px-4 py-2 w-full hover:bg-gray-100 dark:hover:bg-gray-600 dark:hover:text-white"
              >
                Cancella
              </button>
            </li>
          </ul>
        </div>
      </div>
    </>
  );
};
