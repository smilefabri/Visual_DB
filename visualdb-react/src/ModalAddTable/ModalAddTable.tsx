import { FC, useState } from "react";
import axios from "axios";

interface Table {
  id: string;
  nome: string;
  data_creazione: string;
  database: string;
  ora_creazione: string;
}

interface ModalProps {
  onClose: () => void;
  database: string | undefined;
  listOfTables: Table[];
  setListOfTables: (listOfTables: Table[]) => void
}

interface ResponseData {
  operation: string;
  success: boolean;
  Message: string;
  idtable: string; // Assumendo che idtable sia di tipo numerico, adatta al tuo caso
}

export const ModalAddTable: FC<ModalProps> = ({ onClose, database, listOfTables, setListOfTables }: ModalProps) => {
  const [tableName, setTableName] = useState<string>("");

  const handleSubmitNewTable = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    try {
      console.error(database);
      const jwtoken = "Bearer $" + sessionStorage.getItem("jwtToken");
      console.debug(jwtoken);
      const data = await axios.post<ResponseData>(
        "http://localhost:8080/visualdb-api/table/create",
        {
          body: {
            NameTable: tableName,
            idDb: database,
          },
        },
        {
          headers: {
            Authorization: jwtoken,
            "Content-Type": "application/json",
          },
        }
      );
      if (data && data.data) {
        const responseData: ResponseData = data.data;
        const jsonResult: ResponseData = responseData;
        const newTable: Table = {
          id: jsonResult.idtable,
          nome: tableName,
          data_creazione: "0",
          database: database || "",
          ora_creazione: "0",
        }
        const newLista = [...listOfTables, newTable];
        setListOfTables(newLista);
        onClose();
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
    <div className="fixed z-20 top-0 left-0 w-full h-full flex items-center justify-center bg-gray-800 bg-opacity-75">
      <div className="bg-gray-950 p-8 rounded-lg">
        <form className="max-w-sm mx-auto" onSubmit={handleSubmitNewTable}>
          <div className="mb-5">
            <label
                htmlFor="table"
                className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
            >
              Nome della tabella
            </label>
            <label
                htmlFor="table"
                className="block mb-2 text-sm font-light text-gray-600 dark:text-gray-600"
            >
              * gli spazi vuoti verranno sostituiti con degli underscore
            </label>
            <input
                type="table"
                id="table"
                className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                placeholder="es. Utenti"
                required
                onChange={(e) => setTableName(e.target.value)}
            />
          </div>
          <button
              type="submit"
              className="text-white bg-gradient-to-r from-blue-500 via-blue-600 to-blue-700 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-blue-300 dark:focus:ring-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2"
          >
            Submit
          </button>
          <button
            onClick={onClose}
            className="text-white bg-gradient-to-r from-red-400 via-red-500 to-red-600 hover:bg-gradient-to-br focus:ring-4 focus:outline-none focus:ring-red-300 dark:focus:ring-red-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2"
          >
            Annulla
          </button>
        </form>
      </div>
    </div>
  );
};
